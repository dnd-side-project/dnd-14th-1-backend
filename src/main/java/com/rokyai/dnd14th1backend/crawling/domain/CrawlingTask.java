package com.rokyai.dnd14th1backend.crawling.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.common.util.UuidV7;
import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 크롤링 작업 엔티티 정의 */
@Entity
@Table(name = "crawling_tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrawlingTask {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CrawlingStatus status = CrawlingStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 크롤링 작업 생성
     *
     * @param sourceUrl 크롤링 대상 URL
     * @param platform 플랫폼
     * @param userId 사용자 ID (nullable)
     * @return 생성된 CrawlingTask
     */
    public static CrawlingTask create(String sourceUrl, Platform platform, UUID userId) {
        return CrawlingTask.builder()
                .sourceUrl(sourceUrl)
                .platform(platform)
                .userId(userId)
                .status(CrawlingStatus.PENDING)
                .build();
    }

    /** 작업 상태를 [진행 중]으로 변경 */
    public void markInProgress() {
        this.status = CrawlingStatus.IN_PROGRESS;
    }

    /** 작업 상태를 [완료]로 변경 */
    public void markCompleted() {
        this.status = CrawlingStatus.COMPLETED;
    }

    /**
     * 작업 상태를 [실패]로 변경
     *
     * @param errorMessage 에러 메시지
     */
    public void markFailed(String errorMessage) {
        this.status = CrawlingStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
