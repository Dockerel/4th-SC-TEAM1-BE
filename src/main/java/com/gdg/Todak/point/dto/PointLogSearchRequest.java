package com.gdg.Todak.point.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PointLogSearchRequest {
    private String pointType;
    private String pointStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}
