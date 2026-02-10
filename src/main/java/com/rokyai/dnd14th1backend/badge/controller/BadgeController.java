package com.rokyai.dnd14th1backend.badge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.service.BadgeService;

/** 배지 API. */
@RestController
@RequestMapping(version = "0.0.1", path = "/api/badges")
@RequiredArgsConstructor
@Tag(name = "배지", description = "배지 조회 API")
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 전체 배지 목록을 조회합니다.
     *
     * @return 전체 배지 목록
     */
    @GetMapping
    @Operation(summary = "전체 배지 목록 조회", description = "등록된 모든 배지를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
            })
    public List<BadgeResponse> getAllBadges() {
        return badgeService.getAllBadges();
    }
}
