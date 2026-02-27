package com.rokyai.dnd14th1backend.badge.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.rokyai.dnd14th1backend.badge.dto.GrantTestBadgeRequest;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.service.TestBadgeService;

/** 테스트 배지 부여 API. dev 환경에서만 활성화됩니다. */
@RestController
@Profile("dev")
@RequestMapping("/open-api/v1/test")
@Tag(name = "테스트 배지", description = "테스트 배지 부여 API (dev 환경 전용)")
public class TestBadgeController {

    private final TestBadgeService testBadgeService;

    public TestBadgeController(TestBadgeService testBadgeService) {
        this.testBadgeService = testBadgeService;
    }

    /**
     * 특정 유저에게 배지를 부여하고, 해당 배지의 획득 조건에 맞게 진행 데이터를 조정합니다.
     *
     * @param request 배지 부여 요청 (userId, badgeId)
     * @return 부여된 배지 정보
     */
    @PostMapping("/badges")
    @Operation(
            summary = "테스트 배지 부여",
            description =
                    "dev 환경에서만 사용 가능한 테스트 배지 부여 API. "
                            + "배지 부여와 함께 해당 배지의 triggerType에 맞는 진행 데이터(XP, 최적화 횟수 등)도 조정됩니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "배지 부여 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(implementation = UserBadgeResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (사용자/배지 없음, 이미 보유)"),
            })
    public ResponseEntity<UserBadgeResponse> grantTestBadge(
            @Valid @RequestBody GrantTestBadgeRequest request) {
        UserBadgeResponse response = testBadgeService.grantBadge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
