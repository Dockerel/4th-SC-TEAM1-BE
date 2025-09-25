package com.gdg.Todak.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateAccessTokenResponse {
    private String accessToken;

    @Builder
    public UpdateAccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public static UpdateAccessTokenResponse of(String accessToken) {
        return UpdateAccessTokenResponse.builder().accessToken(accessToken).build();
    }
}
