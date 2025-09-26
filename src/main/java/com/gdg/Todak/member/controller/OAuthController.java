package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.service.OAuthService;
import com.gdg.Todak.member.service.dto.KakaoAuthenticationCodeDto;
import com.gdg.Todak.member.service.dto.NaverAuthenticationCodeDto;
import com.gdg.Todak.member.service.response.LoginResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.gdg.Todak.common.exception.errors.MemberError.NAVER_OAUTH_STATE_ERROR;
import static com.gdg.Todak.member.util.AuthConst.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
@RestController
@Tag(name = "OAUTH 인증", description = "OAUTH 인증 관련 API")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login/naver")
    public ApiResponse<String> getNaverAuthenticationCode(HttpServletRequest request) {
        NaverAuthenticationCodeDto naverAuthenticationCodeDto = oAuthService.getNaverAuthenticationCode();

        HttpSession session = request.getSession(true);
        session.setAttribute(NAVER_OAUTH_STATE, naverAuthenticationCodeDto.getState());

        return ApiResponse.ok(naverAuthenticationCodeDto.getUrl());
    }

    @GetMapping("/login/naver/callback")
    public ApiResponse<String> getNaverAccessCode(
            @RequestParam("state") String state,
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        HttpSession session = request.getSession(false);
        String naverOauthState = (String) session.getAttribute(NAVER_OAUTH_STATE);

        if (naverOauthState == null || state == null || !naverOauthState.equals(state)) {
            throw new TodakException(NAVER_OAUTH_STATE_ERROR);
        }

        LoginResponse tokens = oAuthService.naverLogin(state, code);

        ResponseCookie refresh = createResponseCookie(tokens.getRefreshToken());
        response.addHeader(SET_COOKIE, refresh.toString());

        return ApiResponse.ok(tokens.getAccessToken());
    }

    @GetMapping("/login/kakao")
    public ApiResponse<String> getKakaoAuthenticationCode() {
        KakaoAuthenticationCodeDto kakaoAuthenticationCodeDto = oAuthService.getKakaoAuthenticationCode();
        return ApiResponse.ok(kakaoAuthenticationCodeDto.getUrl());
    }

    @GetMapping("/login/kakao/callback")
    public ApiResponse<String> getKakaoAccessCode(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        LoginResponse tokens = oAuthService.kakaoLogin(code);

        ResponseCookie refresh = createResponseCookie(tokens.getRefreshToken());
        response.addHeader(SET_COOKIE, refresh.toString());

        return ApiResponse.ok(tokens.getAccessToken());
    }

    private ResponseCookie createResponseCookie(String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        return cookie;
    }

}
