package com.rokyai.dnd14th1backend.badge.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 테스트 배지 부여 요청. dev 환경에서만 사용 가능합니다. */
@Schema(description = "테스트 배지 부여 요청 (dev 환경 전용)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantTestBadgeRequest {

    @Schema(description = "배지를 부여할 사용자 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull private UUID userId;

    @Schema(description = "부여할 배지 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull private UUID badgeId;
}
