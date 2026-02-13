package com.rokyai.dnd14th1backend.crawling.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Chat 목록 응답 DTO */
@Schema(description = "Chat 목록 응답")
public record ChatListResponse(
        @Schema(description = "대화 ID") UUID conversationId,
        @Schema(description = "대화 제목") String title,
        @Schema(description = "Chat 목록") List<ChatResponse> chats) {}
