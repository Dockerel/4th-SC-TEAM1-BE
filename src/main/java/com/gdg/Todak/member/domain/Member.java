package com.gdg.Todak.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String password;

    private String nickname;

    private String imageUrl;

    private String salt;

    private boolean isSocialAccount;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MemberRole> memberRoles = new HashSet<>();

    @Builder
    public Member(String userId, String password, String nickname, String imageUrl, String salt) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.salt = salt;
        this.isSocialAccount = false;
    }

    public static Member of(String userId, String nickname, String imageUrl) {
        String string = UUID.randomUUID().toString();
        return Member.of(userId, string, nickname, imageUrl, string);
    }

    public static Member of(String userId, String password, String nickname, String imageUrl, String salt) {
        return Member.builder()
            .userId(userId)
            .password(password)
            .nickname(nickname)
            .imageUrl(imageUrl)
            .salt(salt)
            .build();
    }

    public void addRole(MemberRole memberRole) {
        memberRoles.add(memberRole);
    }

    public Set<Role> getRoles() {
        return memberRoles.stream()
            .map(MemberRole::getRole).collect(Collectors.toSet());
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSocialAccount() {
        isSocialAccount = true;
    }
}
