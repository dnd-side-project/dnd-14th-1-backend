package com.rokyai.dnd14th1backend.crawling.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.common.util.UuidV7;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 대화 세션 엔티티 */
@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawling_task_id", columnDefinition = "UUID")
    private CrawlingTask crawlingTask;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(length = 500)
    private String title;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Platform platform;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<Chat> chats = new ArrayList<>();

    /**
     * 대화 세션생성
     *
     * @param crawlingTask 크롤링 작업
     * @param title 대화 제목
     * @return 생성된 Conversation
     */
    public static Conversation create(CrawlingTask crawlingTask, String title) {
        return Conversation.builder()
                .crawlingTask(crawlingTask)
                .userId(crawlingTask.getUserId())
                .title(title)
                .sourceUrl(crawlingTask.getSourceUrl())
                .platform(crawlingTask.getPlatform())
                .build();
    }

    /**
     * 사용자 생성 Conversation
     *
     * @param userId 사용자 ID
     * @param title 대화 제목
     * @return 생성된 Conversation
     */
    public static Conversation createByUser(UUID userId, String title) {
        return Conversation.builder().userId(userId).title(title).build();
    }

    /**
     * 메시지 추가
     *
     * @param message 추가할 메시지
     */
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    /**
     * Chat 추가
     *
     * @param chat 추가할 Chat
     */
    public void addChat(Chat chat) {
        this.chats.add(chat);
    }
}
