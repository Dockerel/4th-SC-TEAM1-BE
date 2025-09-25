package com.gdg.Todak.member.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import com.gdg.Todak.member.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gdg.Todak.common.exception.errors.MemberError.INVALID_TOKEN_ERROR;
import static com.gdg.Todak.common.exception.errors.MemberError.MEMBER_NOT_FOUND_ERROR;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public Jwt updateAccessToken(UpdateAccessTokenServiceRequest request) {
        String accessToken = request.getAccessToken();

        Member member = getMember(accessToken);

        String memberId = member.getId().toString();
        String refreshToken = (String) redisTemplate.opsForValue().get(memberId);

        if (refreshToken == null) {
            throw new TodakException(INVALID_TOKEN_ERROR);
        }

        if (!refreshToken.equals(request.getRefreshToken())) {
            throw new TodakException(INVALID_TOKEN_ERROR);
        }

        String newAccessToken = createNewAccessToken(member);
        String newRefreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(newRefreshToken, member);

        return Jwt.of(newAccessToken, newRefreshToken);
    }

    private Member getMember(String accessToken) {
        String userId = jwtProvider.getUserIdForReissue(accessToken)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_ERROR));
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_ERROR));
        return member;
    }

    private String createNewAccessToken(Member member) {
        Map<String, Object> claims = jwtProvider.createClaims(member, member.getRoles());

        String accessToken = jwtProvider.createAccessToken(claims);
        return accessToken;
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        String memberId = member.getId().toString();
        redisTemplate.opsForValue().set(memberId, refreshToken, 14, TimeUnit.DAYS);
    }
}
