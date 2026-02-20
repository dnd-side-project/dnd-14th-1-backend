package com.rokyai.dnd14th1backend.auth.controller;

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

import com.rokyai.dnd14th1backend.auth.dto.AppleOAuthRequest;
import com.rokyai.dnd14th1backend.auth.dto.AuthResponse;
import com.rokyai.dnd14th1backend.auth.dto.RefreshTokenRequest;
import com.rokyai.dnd14th1backend.auth.dto.TokenRefreshResponse;
import com.rokyai.dnd14th1backend.auth.service.AppleOAuthService;
import com.rokyai.dnd14th1backend.auth.service.TokenService;

/** 인증 관련 API 엔드포인트. */
@RestController
@RequestMapping("/open-api/v1/auth")
@Tag(name = "인증", description = "OAuth 인증 관련 API")
public class AuthController {

    private final AppleOAuthService appleOAuthService;
    private final TokenService tokenService;

    public AuthController(AppleOAuthService appleOAuthService, TokenService tokenService) {
        this.appleOAuthService = appleOAuthService;
        this.tokenService = tokenService;
    }

    /**
     * Apple OAuth를 통해 로그인합니다.
     *
     * @param request Apple OAuth 요청
     * @return 액세스 토큰과 사용자 정보
     */
    @PostMapping("/apple")
    @Operation(summary = "Apple OAuth 로그인", description = "Apple ID Token을 이용한 OAuth 로그인")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "로그인 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AuthResponse.class))),
                @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 또는 ID Token"),
                @ApiResponse(responseCode = "500", description = "서버 오류"),
            })
    public ResponseEntity<AuthResponse> loginWithApple(
            @Valid @RequestBody AppleOAuthRequest request) {
        AuthResponse response = appleOAuthService.authenticateWithApple(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Refresh Token으로 새로운 토큰 쌍을 재발급합니다.
     *
     * @param request 리프레시 토큰 요청
     * @return 새로운 액세스 토큰과 리프레시 토큰
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 재발급합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "토큰 재발급 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                TokenRefreshResponse.class))),
                @ApiResponse(
                        responseCode = "401",
                        description = "유효하지 않은 Refresh Token 또는 만료된 토큰"),
            })
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = tokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
