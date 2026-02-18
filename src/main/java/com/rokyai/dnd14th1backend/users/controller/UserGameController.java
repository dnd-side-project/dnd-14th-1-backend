package com.rokyai.dnd14th1backend.users.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.rokyai.dnd14th1backend.users.dto.UserGameProfileResponse;
import com.rokyai.dnd14th1backend.users.service.UserGameService;

/** 사용자 게임 프로필 컨트롤러 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "사용자", description = "사용자 게임 프로필 API")
public class UserGameController {

    private final UserGameService userGameService;

    public UserGameController(UserGameService userGameService) {
        this.userGameService = userGameService;
    }

    @GetMapping("/profile")
    @Operation(summary = "내 게임 프로필 조회", description = "현재 사용자의 XP, 티어 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                UserGameProfileResponse.class))),
                @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
            })
    public ResponseEntity<UserGameProfileResponse> getProfile(
            @AuthenticationPrincipal UUID userId) {
        UserGameProfileResponse response = userGameService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}
