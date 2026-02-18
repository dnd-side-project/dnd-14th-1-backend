package com.rokyai.dnd14th1backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 테스트 사용자 생성 요청. dev 환경에서만 사용 가능합니다. */
@Schema(description = "테스트 사용자 생성 요청 (dev 환경 전용)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTestUserRequest {

    @Schema(description = "테스트 사용자 이메일", example = "test@example.com", nullable = true)
    @Email
    private String email;
}
