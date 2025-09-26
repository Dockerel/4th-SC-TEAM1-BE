package com.gdg.Todak.member.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class KakaoAuthenticationCodeDto {
    private String url;

    public static KakaoAuthenticationCodeDto of(String url) {
        return KakaoAuthenticationCodeDto.builder()
                .url(url)
                .build();
    }
}
