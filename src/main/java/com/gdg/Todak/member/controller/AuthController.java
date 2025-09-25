package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.controller.dto.UpdateAccessTokenResponse;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.gdg.Todak.member.util.AuthConst.REFRESH_TOKEN;
import static com.gdg.Todak.member.util.AuthConst.SET_COOKIE;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    @Operation(summary = "액세스 토큰 갱신", description = "리프레시 토큰이 유효하다면 액세스 토큰을 갱신한다.")
    public ApiResponse<UpdateAccessTokenResponse> updateAccessTokenToken(
            @CookieValue(name = REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        Jwt jwt = authService.updateAccessToken(refreshToken);

        ResponseCookie refresh = ResponseCookie.from(REFRESH_TOKEN, jwt.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        response.addHeader(SET_COOKIE, refresh.toString());

        return ApiResponse.ok(UpdateAccessTokenResponse.of(jwt.getAccessToken()));
    }

}
