package com.rokyai.dnd14th1backend.crawling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Conversation 생성 요청 DTO */
@Schema(description = "Conversation 생성 요청")
public record CreateConversationRequest(
        @Schema(description = "대화 제목") @NotBlank @Size(max = 500) String title) {}
