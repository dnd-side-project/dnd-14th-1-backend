package com.rokyai.dnd14th1backend.crawling.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.common.util.UuidV7;
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;

/** 개별 메시지 엔티티 */
@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false, columnDefinition = "UUID")
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer sequence;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 메시지 생성
     *
     * @param conversation 대화 세션
     * @param role 역할 (USER/ASSISTANT)
     * @param content 메시지 내용
     * @param sequence 메시지 순서
     * @return 생성된 Message
     */
    public static Message create(
            Conversation conversation, MessageRole role, String content, int sequence) {
        return Message.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .sequence(sequence)
                .build();
    }
}
