package com.gdg.Todak.member.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoOAuthAccessTokenDto(
        @JsonProperty("access_token")
        String accessToken
) {
}
