package com.rokyai.dnd14th1backend.crawling.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Chat 엔티티, Chat = 한 번의 (사용자) 질의 + (AI) 응답 쌍 */
@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false, columnDefinition = "UUID")
    private Conversation conversation;

    @Column(name = "user_content", nullable = false, columnDefinition = "TEXT")
    private String userContent;

    @Column(name = "assistant_content", columnDefinition = "TEXT")
    private String assistantContent;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "token_saving")
    private Integer tokenSaving;

    @Column(name = "xp_earned")
    private Integer xpEarned;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Chat 생성
     *
     * @param conversation 대화 세션
     * @param userContent 사용자 질의 내용
     * @param assistantContent 응답 내용 (nullable)
     * @param sequence 순서 (1-based)
     * @return 생성된 Chat
     */
    public static Chat create(
            Conversation conversation, String userContent, String assistantContent, int sequence) {
        return Chat.builder()
                .conversation(conversation)
                .userContent(userContent)
                .assistantContent(assistantContent)
                .sequence(sequence)
                .build();
    }

    public void applyOptimization(int tokenSaving, int xpEarned) {
        this.tokenSaving = tokenSaving;
        this.xpEarned = xpEarned;
    }

    public boolean isOptimized() {
        return this.xpEarned != null;
    }
}
