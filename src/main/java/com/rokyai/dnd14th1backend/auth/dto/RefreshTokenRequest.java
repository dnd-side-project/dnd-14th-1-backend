package com.rokyai.dnd14th1backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 토큰 재발급 요청. */
@Schema(description = "토큰 재발급 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @Schema(
            description = "리프레시 토큰",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "refreshToken은 필수입니다")
    private String refreshToken;
}
