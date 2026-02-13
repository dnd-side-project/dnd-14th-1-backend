package com.rokyai.dnd14th1backend.crawling.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.domain.Message;
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;

/** 메시지 응답 DTO */
@Schema(description = "메시지 응답")
public record MessageResponse(
        @Schema(description = "메시지 ID") UUID id,
        @Schema(description = "역할 (USER/ASSISTANT)") MessageRole role,
        @Schema(description = "메시지 내용") String content,
        @Schema(description = "메시지 순서") Integer sequence) {

    /**
     * Message에서 응답 생성
     *
     * @param message 메시지 엔티티
     * @return 메시지 응답
     */
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(), message.getRole(), message.getContent(), message.getSequence());
    }
}
