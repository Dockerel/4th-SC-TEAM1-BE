package com.gdg.Todak.member.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoOAuthUserInfoDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("properties")
        Properties properties
) {
    public record Properties(
            @JsonProperty("nickname")
            String nickname,
            @JsonProperty("profile_image")
            String profileImage
    ) {
    }
}
