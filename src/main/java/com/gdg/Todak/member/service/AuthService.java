package com.gdg.Todak.member.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gdg.Todak.common.exception.errors.MemberError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public Jwt updateAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new TodakException(EXPIRED_REFRESH_TOKEN_ERROR);
        }

        String memberIdString = (String) redisTemplate.opsForValue().get(refreshToken);

        if (memberIdString == null) {
            throw new TodakException(INVALID_TOKEN_ERROR);
        }

        Long memberId = Long.valueOf(memberIdString);

        redisTemplate.delete(refreshToken);

        String newAccessToken = createNewAccessToken(memberId);
        String newRefreshToken = jwtProvider.createRefreshToken();

        redisTemplate.opsForValue().set(newRefreshToken, memberId, 14, TimeUnit.DAYS);

        return Jwt.of(newAccessToken, newRefreshToken);
    }

    private String createNewAccessToken(Long memberId) {
        Member member = getMember(memberId);

        Map<String, Object> claims = jwtProvider.createClaims(member, member.getRoles());

        String accessToken = jwtProvider.createAccessToken(claims);
        return accessToken;
    }

    private Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_ERROR));
        return member;
    }

}
