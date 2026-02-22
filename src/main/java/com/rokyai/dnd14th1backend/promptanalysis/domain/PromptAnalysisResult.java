package com.rokyai.dnd14th1backend.promptanalysis.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

import com.rokyai.dnd14th1backend.common.util.UuidV7;

/** 프롬프트 분석 결과 엔티티 */
@Entity
@Table(name = "prompt_analysis_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PromptAnalysisResult {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    /** 분석을 요청한 사용자 ID */
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    /** 원본 대비 개선 프롬프트의 추정 토큰 절약량 */
    @Column(name = "estimated_token_saving", nullable = false)
    private Integer estimatedTokenSaving;

    /** 절약 토큰 기반 빙하 녹음 감소량 (kg 단위) */
    @Column(name = "glacier_melt_reduction_kg", nullable = false)
    private Double glacierMeltReductionKg;

    /** 프롬프트가 이미 충분히 정제되어 개선사항이 없을 때 true */
    @Column(name = "no_improvement", nullable = false)
    private Boolean noImprovement;

    /** 프롬프트가 이해 불가하여 개선이 어려울 때 true */
    @Column(name = "cannot_improve", nullable = false)
    private Boolean cannotImprove;

    /** 이번 분석으로 획득한 XP (noImprovement/cannotImprove 시 0) */
    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    /** 프롬프트 개선 제안 목록 */
    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromptAnalysisSuggestion> suggestions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addSuggestions(List<PromptAnalysisSuggestion> suggestions) {
        this.suggestions.addAll(suggestions);
        suggestions.forEach(s -> s.assignResult(this));
    }
}
