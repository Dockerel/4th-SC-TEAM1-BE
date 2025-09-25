package com.gdg.Todak.member.Interceptor;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.gdg.Todak.common.exception.errors.MemberError.INVALID_TOKEN_ERROR;
import static com.gdg.Todak.member.util.JwtConstants.AUTHORIZATION;
import static com.gdg.Todak.member.util.JwtConstants.BEARER;

@RequiredArgsConstructor
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        String header = request.getHeader(AUTHORIZATION);
        if (isNotValidToken(header)) {
            throw new TodakException(INVALID_TOKEN_ERROR);
        }

        if (isNotValidClaim(header)) {
            throw new TodakException(INVALID_TOKEN_ERROR);
        }

        return true;
    }

    private boolean isNotValidToken(String header) {
        return header == null || !header.startsWith(BEARER);
    }

    private boolean isNotValidClaim(String header) {
        String token = header.substring(7);
        Claims claims = jwtProvider.getClaims(token);
        return claims == null;
    }
}
