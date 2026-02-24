package com.rokyai.dnd14th1backend.users.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** 내 정보 조회 응답. */
@Schema(description = "내 정보")
public record UserMeResponse(
        @Schema(description = "사용자 ID") UUID userId,
        @Schema(description = "이메일") String email,
        @Schema(description = "사용자 이름") String name,
        @Schema(description = "가입 일시") LocalDateTime createdAt,
        @Schema(description = "대표 배지 (미설정 시 null)")
                RepresentativeBadgeResponse representativeBadge) {}
