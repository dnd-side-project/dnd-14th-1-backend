package com.rokyai.dnd14th1backend.crawling.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.domain.Conversation;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 대화 응답 DTO. */
@Schema(description = "대화 응답")
public record ConversationResponse(
        @Schema(description = "대화 ID") UUID id,
        @Schema(description = "대화 제목") String title,
        @Schema(description = "원본 URL") String sourceUrl,
        @Schema(description = "플랫폼") Platform platform,
        @Schema(description = "메시지 목록") List<MessageResponse> messages,
        @Schema(description = "생성 시각") LocalDateTime createdAt) {

    /**
     * Conversation에서 응답을 생성합니다.
     *
     * @param conversation 대화 엔티티
     * @return 대화 응답
     */
    public static ConversationResponse from(Conversation conversation) {
        List<MessageResponse> messageResponses =
                conversation.getMessages().stream().map(MessageResponse::from).toList();

        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getSourceUrl(),
                conversation.getPlatform(),
                messageResponses,
                conversation.getCreatedAt());
    }
}
