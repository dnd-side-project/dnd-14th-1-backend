package com.rokyai.dnd14th1backend.promptanalysis.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.rokyai.dnd14th1backend.common.pagination.CursorPageRequest;
import com.rokyai.dnd14th1backend.common.pagination.CursorPageResponse;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultRequest;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultResponse;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary;
import com.rokyai.dnd14th1backend.promptanalysis.service.PromptAnalysisService;

/** 프롬프트 분석 결과 API 컨트롤러 */
@RestController
@RequestMapping("/api/v1/prompt-analysis")
@Tag(name = "프롬프트 분석", description = "AI 프롬프트 분석 결과 API")
public class PromptAnalysisController {

    private final PromptAnalysisService promptAnalysisService;

    public PromptAnalysisController(PromptAnalysisService promptAnalysisService) {
        this.promptAnalysisService = promptAnalysisService;
    }

    /**
     * 프롬프트 분석 결과 제출 + XP 적립 + 배지 체크
     *
     * @param request 분석 결과 요청
     * @param userId 사용자 ID
     * @return 분석 결과 응답 (XP, 티어, 배지 정보)
     */
    @PostMapping("/results")
    @Operation(
            summary = "프롬프트 분석 결과 제출",
            description =
                    "Apple Intelligence 프롬프트 분석 결과를 제출하고 XP를 적립합니다."
                            + " noImprovement=true 또는 cannotImprove=true일 경우 XP/배지 미적립")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "분석 결과 제출 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                PromptAnalysisResultResponse
                                                                        .class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    public ResponseEntity<PromptAnalysisResultResponse> submitResult(
            @Valid @RequestBody PromptAnalysisResultRequest request,
            @AuthenticationPrincipal UUID userId) {
        PromptAnalysisResultResponse response = promptAnalysisService.submitResult(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 프롬프트 분석 결과 목록 조회 (cursor 기반 페이지네이션)
     *
     * @param pageRequest cursor 페이지네이션 요청
     * @param userId 사용자 ID
     * @return 분석 결과 목록 (최신순)
     */
    @GetMapping("/results")
    @Operation(
            summary = "내 분석 결과 목록 조회",
            description = "현재 사용자의 프롬프트 분석 결과를 cursor 기반 페이지네이션으로 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    public ResponseEntity<CursorPageResponse<PromptAnalysisResultSummary>> getResults(
            @Valid CursorPageRequest pageRequest, @AuthenticationPrincipal UUID userId) {
        CursorPageResponse<PromptAnalysisResultSummary> response =
                promptAnalysisService.getResults(userId, pageRequest);
        return ResponseEntity.ok(response);
    }
}
