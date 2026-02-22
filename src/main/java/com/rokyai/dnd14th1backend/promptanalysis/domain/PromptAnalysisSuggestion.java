package com.rokyai.dnd14th1backend.promptanalysis.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

import com.rokyai.dnd14th1backend.common.util.UuidV7;

/** 프롬프트 분석 개선 제안 엔티티 */
@Entity
@Table(name = "prompt_analysis_suggestions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PromptAnalysisSuggestion {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    /** 이 제안이 속한 분석 결과 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false, columnDefinition = "UUID")
    private PromptAnalysisResult result;

    /** 사용자 입력 중 모호하거나 개선이 필요한 원본 텍스트 부분 */
    @Column(name = "original_part", nullable = false, columnDefinition = "TEXT")
    private String originalPart;

    /** LLM이 문맥을 더 잘 이해할 수 있도록 구체화하고 명확하게 수정한 텍스트 */
    @Column(name = "improved_part", nullable = false, columnDefinition = "TEXT")
    private String improvedPart;

    /** 수정 이유에 대한 논리적이고 구체적인 설명 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    void assignResult(PromptAnalysisResult result) {
        this.result = result;
    }
}
