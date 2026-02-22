package com.rokyai.dnd14th1backend.badge.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.dto.RepresentBadgeRequest;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.service.BadgeService;

/** 유저 배지 인증 API. */
@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
@Tag(name = "배지", description = "유저 배지 조회 API")
public class UserBadgeController {

    private final BadgeService badgeService;

    /**
     * 내 보유 배지 목록을 조회합니다.
     *
     * @param userId 인증된 사용자 ID
     * @return 보유 배지 목록
     */
    @GetMapping("/my")
    @Operation(summary = "내 배지 목록 조회", description = "현재 로그인한 사용자가 획득한 모든 배지를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    public List<UserBadgeResponse> getMyBadges(@AuthenticationPrincipal UUID userId) {
        return badgeService.getUserBadges(userId);
    }

    /**
     * 대표 배지를 설정합니다.
     *
     * @param userId 인증된 사용자 ID
     * @param request 대표 배지 설정 요청
     */
    @PutMapping("/representative")
    @Operation(summary = "대표 배지 설정", description = "획득한 배지 중 하나를 대표 배지로 설정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "설정 성공"),
                @ApiResponse(responseCode = "400", description = "미획득 배지 설정 시도"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    public void setRepresentativeBadge(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody RepresentBadgeRequest request) {
        badgeService.setRepresentativeBadge(userId, request.badgeId());
    }
}
