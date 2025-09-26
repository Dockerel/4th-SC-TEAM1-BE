package com.gdg.Todak.member.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverOAuthUserInfoDto(
        Response response
) {
    public record Response(
            @JsonProperty("id")
            String id,
            @JsonProperty("nickname")
            String nickname,
            @JsonProperty("profile_image")
            String profileImage
    ) {
    }
}
