package com.rokyai.dnd14th1backend.crawling.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.domain.Chat;

/** Chat 단일 응답 DTO */
@Schema(description = "Chat 응답")
public record ChatResponse(
        @Schema(description = "Chat ID") UUID id,
        @Schema(description = "Conversation ID") UUID conversationId,
        @Schema(description = "사용자 질의 내용") String userContent,
        @Schema(description = "응답 내용") String assistantContent,
        @Schema(description = "순서") int sequence,
        @Schema(description = "생성 시각") LocalDateTime createdAt) {

    /**
     * Chat 엔티티에서 응답 생성
     *
     * @param chat Chat 엔티티
     * @return Chat 응답
     */
    public static ChatResponse from(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getConversation().getId(),
                chat.getUserContent(),
                chat.getAssistantContent(),
                chat.getSequence(),
                chat.getCreatedAt());
    }
}
