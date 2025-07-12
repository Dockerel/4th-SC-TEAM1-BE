package com.gdg.Todak.point.dto;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointType;

public record PointRequest(
        Member member,
        PointType pointType
) {
    public static PointRequest of(Member member, PointType pointType) {
        return new PointRequest(member, pointType);
    }
}
