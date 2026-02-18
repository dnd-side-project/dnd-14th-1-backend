package com.rokyai.dnd14th1backend.auth.controller;

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

import com.rokyai.dnd14th1backend.auth.dto.AuthResponse;
import com.rokyai.dnd14th1backend.auth.dto.CreateTestUserRequest;
import com.rokyai.dnd14th1backend.auth.service.TestAuthService;

/** 테스트 인증 API. dev 환경에서만 활성화됩니다. */
@RestController
@Profile("dev")
@RequestMapping("/open-api/v1/test")
@Tag(name = "테스트 인증", description = "테스트 사용자 생성 API (dev 환경 전용)")
public class TestAuthController {

    private final TestAuthService testAuthService;

    public TestAuthController(TestAuthService testAuthService) {
        this.testAuthService = testAuthService;
    }

    /**
     * 테스트 사용자를 생성하고 인증 토큰을 반환합니다.
     *
     * @param request 테스트 사용자 생성 요청
     * @return 액세스 토큰과 사용자 정보
     */
    @PostMapping("/users")
    @Operation(
            summary = "테스트 사용자 생성",
            description = "dev 환경에서만 사용 가능한 테스트 사용자 생성 API. 이메일 미입력 시 랜덤 이메일로 생성됩니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "테스트 사용자 생성 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AuthResponse.class))),
            })
    public ResponseEntity<AuthResponse> createTestUser(
            @Valid @RequestBody(required = false) CreateTestUserRequest request) {
        if (request == null) {
            request = new CreateTestUserRequest();
        }
        AuthResponse response = testAuthService.createTestUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
