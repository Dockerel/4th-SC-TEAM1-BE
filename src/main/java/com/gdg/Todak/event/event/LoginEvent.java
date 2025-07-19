package com.gdg.Todak.event.event;

import com.gdg.Todak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginEvent {
    private Member member;

    public static LoginEvent of(Member member) {
        return LoginEvent.builder()
                .member(member)
                .build();
    }
}
