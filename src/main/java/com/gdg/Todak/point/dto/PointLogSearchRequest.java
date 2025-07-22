package com.gdg.Todak.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointLogSearchRequest {
    private String pointType;
    private String pointStatus;
    private LocalDate startDate;
    private LocalDate endDate;

    public static PointLogSearchRequest of(String pointType, String pointStatus, LocalDate startDate, LocalDate endDate) {
        return PointLogSearchRequest.builder()
                .pointType(pointType)
                .pointStatus(pointStatus)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
