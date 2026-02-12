package com.rokyai.dnd14th1backend.crawling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Chat 생성 요청 DTO */
@Schema(description = "Chat 생성 요청")
public record CreateChatRequest(
        @Schema(description = "사용자 질의 내용") @NotBlank String userContent,
        @Schema(description = "응답 내용 (nullable)") String assistantContent) {}
