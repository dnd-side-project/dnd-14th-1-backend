package com.rokyai.dnd14th1backend.promptanalysis.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.promptanalysis.domain.PromptAnalysisResult;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary;

/** 프롬프트 분석 결과 리포지토리 */
@Repository
public interface PromptAnalysisResultRepository extends JpaRepository<PromptAnalysisResult, UUID> {

    /**
     * 유저의 유효한 분석 결과 횟수 조회 (noImprovement=false, cannotImprove=false)
     *
     * @param userId 사용자 ID
     * @return 유효 분석 결과 수
     */
    @Query(
            "SELECT COUNT(r) FROM PromptAnalysisResult r"
                    + " WHERE r.userId = :userId"
                    + " AND r.noImprovement = false"
                    + " AND r.cannotImprove = false")
    long countValidByUserId(@Param("userId") UUID userId);

    /**
     * Cursor 기반 분석 결과 요약 목록 조회 (첫 페이지, DTO 프로젝션)
     *
     * @param userId 사용자 ID
     * @param limit 조회 개수 (size + 1)
     * @return 분석 결과 요약 목록 (최신순)
     */
    @Query(
            "SELECT new com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary("
                    + "r.id, r.estimatedTokenSaving, r.glacierMeltReductionKg, r.xpEarned,"
                    + " r.noImprovement, r.cannotImprove, size(r.suggestions), r.createdAt)"
                    + " FROM PromptAnalysisResult r"
                    + " WHERE r.userId = :userId"
                    + " ORDER BY r.id DESC"
                    + " LIMIT :limit")
    List<PromptAnalysisResultSummary> findSummariesByUserIdOrderByIdDesc(
            @Param("userId") UUID userId, @Param("limit") int limit);

    /**
     * Cursor 기반 분석 결과 요약 목록 조회 (다음 페이지, DTO 프로젝션)
     *
     * @param userId 사용자 ID
     * @param cursor 이전 페이지 마지막 항목 ID
     * @param limit 조회 개수 (size + 1)
     * @return 분석 결과 요약 목록 (최신순)
     */
    @Query(
            "SELECT new com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary("
                    + "r.id, r.estimatedTokenSaving, r.glacierMeltReductionKg, r.xpEarned,"
                    + " r.noImprovement, r.cannotImprove, size(r.suggestions), r.createdAt)"
                    + " FROM PromptAnalysisResult r"
                    + " WHERE r.userId = :userId"
                    + " AND r.id < :cursor"
                    + " ORDER BY r.id DESC"
                    + " LIMIT :limit")
    List<PromptAnalysisResultSummary> findSummariesByUserIdAndIdLessThanOrderByIdDesc(
            @Param("userId") UUID userId, @Param("cursor") UUID cursor, @Param("limit") int limit);
}
