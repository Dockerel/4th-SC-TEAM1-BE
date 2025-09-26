package com.gdg.Todak.member.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NaverAuthenticationCodeDto {
    private String state;
    private String url;

    public static NaverAuthenticationCodeDto of(String state, String url) {
        return NaverAuthenticationCodeDto.builder()
                .state(state)
                .url(url)
                .build();
    }
}
