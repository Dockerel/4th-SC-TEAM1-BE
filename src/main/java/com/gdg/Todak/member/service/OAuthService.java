package com.gdg.Todak.member.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.event.event.LoginEvent;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.MemberRole;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.dto.*;
import com.gdg.Todak.member.service.response.LoginResponse;
import com.gdg.Todak.member.util.JwtProvider;
import com.gdg.Todak.point.service.PointService;
import com.gdg.Todak.tree.business.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.gdg.Todak.common.exception.errors.MemberError.NOT_SOCIAL_ACCOUNT_ERROR;
import static com.gdg.Todak.member.service.dto.KakaoOAuthUserInfoDto.*;
import static com.gdg.Todak.member.service.dto.NaverOAuthUserInfoDto.Response;
import static org.springframework.http.HttpHeaders.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OAuthService {

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;
    @Value("${NAVER_CLIENT_SECRET}")
    private String naverClientSecret;
    @Value("${NAVER_CALLBACK_URL}")
    private String naverCallbackUrl;

    @Value("${KAKAO_CLIENT_ID}")
    private String kakaoClientId;
    @Value("${KAKAO_CLIENT_SECRET}")
    private String kakaoClientSecret;
    @Value("${KAKAO_CALLBACK_URL}")
    private String kakaoCallbackUrl;

    private final RestTemplate restTemplate;
    private final RedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final PointService pointService;
    private final TreeService treeService;
    private final JwtProvider jwtProvider;

    public NaverAuthenticationCodeDto getNaverAuthenticationCode() {
        String state = generateState();

        String baseUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&state=%s";

        String url = String.format(baseUrl, naverClientId, naverCallbackUrl, state);

        return NaverAuthenticationCodeDto.of(state, url);
    }

    @Transactional
    public LoginResponse naverLogin(String state, String code) {
        String naverAccessToken = getNaverAccessToken(state, code);

        SocialUserInfoDto userInfo = getNaverUserInfo(naverAccessToken);

        Member member = getOrSignUpMember(userInfo);

        Set<Role> roles = member.getRoles();

        Map<String, Object> claims = jwtProvider.createClaims(member, roles);

        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(refreshToken, member);

        eventPublisher.publishEvent(LoginEvent.of(member));

        return LoginResponse.of(accessToken, refreshToken);
    }

    public KakaoAuthenticationCodeDto getKakaoAuthenticationCode() {
        String baseUrl = "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code";

        String url = String.format(baseUrl, kakaoClientId, kakaoCallbackUrl);

        return KakaoAuthenticationCodeDto.of(url);
    }

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        String kakaoAccessToken = getKakaoAccessToken(code);

        SocialUserInfoDto userInfo = getKakaoUserInfo(kakaoAccessToken);

        Member member = getOrSignUpMember(userInfo);

        Set<Role> roles = member.getRoles();

        Map<String, Object> claims = jwtProvider.createClaims(member, roles);

        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(refreshToken, member);

        eventPublisher.publishEvent(LoginEvent.of(member));

        return LoginResponse.of(accessToken, refreshToken);
    }

    private Member getOrSignUpMember(SocialUserInfoDto info) {
        Member member = memberRepository.findByUserId(info.userId()).orElseGet(() -> {
            Member newMember = Member.of(info.userId(), info.nickname(), info.imageUrl());
            newMember.setSocialAccount();
            memberRepository.save(newMember);

            MemberRole role = MemberRole.of(Role.USER, newMember);
            newMember.addRole(role);

            memberRoleRepository.save(role);

            pointService.createPoint(newMember);

            treeService.getTree(newMember);

            return newMember;
        });

        if (!member.isSocialAccount()) {
            throw new TodakException(NOT_SOCIAL_ACCOUNT_ERROR);
        }

        return member;
    }

    private SocialUserInfoDto getNaverUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<NaverOAuthUserInfoDto> userInfoResponse = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                NaverOAuthUserInfoDto.class
        );

        NaverOAuthUserInfoDto body = userInfoResponse.getBody();
        Response res = body.response();

        return SocialUserInfoDto.of("naver" + res.id(), res.nickname(), res.profileImage());
    }

    private SocialUserInfoDto getKakaoUserInfo(String accessToken) {
        String baseUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<KakaoOAuthUserInfoDto> userInfoResponse = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoOAuthUserInfoDto.class
        );
        KakaoOAuthUserInfoDto body = userInfoResponse.getBody();
        Properties properties = body.properties();

        return SocialUserInfoDto.of("kakao" + body.id(), properties.nickname(), properties.profileImage());
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        String memberId = member.getId().toString();
        redisTemplate.opsForValue().set(refreshToken, memberId, 14, TimeUnit.DAYS);
    }

    private String getNaverAccessToken(String state, String code) {
        String baseUrl = "https://nid.naver.com/oauth2.0/token?client_id=%s&client_secret=%s&grant_type=authorization_code&state=%s&code=%s";

        String url = String.format(baseUrl, naverClientId, naverClientSecret, state, code);

        ResponseEntity<NaverOAuthAccessTokenDto> response = restTemplate.getForEntity(url, NaverOAuthAccessTokenDto.class);

        return response.getBody().accessToken();
    }

    private String getKakaoAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoCallbackUrl);
        body.add("code", code);
        body.add("client_secret", kakaoClientSecret);

        ResponseEntity<KakaoOAuthAccessTokenDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                KakaoOAuthAccessTokenDto.class
        );

        return response.getBody().accessToken();
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

}
