package com.gdg.Todak.member.service.dto;

public record SocialUserInfoDto(
        String userId,
        String nickname,
        String imageUrl
) {
    public static SocialUserInfoDto of(String userId, String nickname, String imageUrl) {
        return new SocialUserInfoDto(userId, nickname, imageUrl);
    }
}
