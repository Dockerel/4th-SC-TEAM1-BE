package com.gdg.Todak.point.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.point.dto.PointResponse;
import com.gdg.Todak.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "포인트 조회", description = "포인트 조회 관련 API")
@RequestMapping("/api/v1/points")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 조회", description = "로그인한 사용자의 포인트를 조회합니다.")
    @GetMapping
    public ApiResponse<PointResponse> getMemberPoint(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        PointResponse pointResponse = pointService.getPoint(authenticateUser.getUserId());
        return ApiResponse.ok(pointResponse);
    }
}
