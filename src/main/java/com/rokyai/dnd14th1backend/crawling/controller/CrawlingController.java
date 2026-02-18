package com.rokyai.dnd14th1backend.crawling.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.rokyai.dnd14th1backend.crawling.dto.ConversationResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingRequest;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingStatusResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingTaskSummary;
import com.rokyai.dnd14th1backend.crawling.service.CrawlingService;

/** 크롤링 API 컨트롤러 */
@RestController
@RequestMapping("/api/v1/crawling")
@Tag(name = "크롤링", description = "AI 챗봇 대화 크롤링 API")
public class CrawlingController {

    private final CrawlingService crawlingService;

    public CrawlingController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    /**
     * AI 대화 크롤링 요청
     *
     * @param request 크롤링 요청
     * @param userId 사용자 ID
     * @return 크롤링 응답
     */
    @PostMapping
    @Operation(
            summary = "크롤링 요청",
            description = "공유 URL로부터 대화 크롤링 (2초 이내 완료 시 결과 반환 / 초과 시 taskId 반환)")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "크롤링 완료 또는 진행 중",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CrawlingResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "500", description = "서버 오류"),
            })
    public ResponseEntity<CrawlingResponse> requestCrawling(
            @Valid @RequestBody CrawlingRequest request, @AuthenticationPrincipal UUID userId) {
        CrawlingResponse response = crawlingService.requestCrawling(request.url(), userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 크롤링 상태 조회
     *
     * @param taskId 작업 ID
     * @param userId 사용자 ID
     * @return 상태 응답
     */
    @GetMapping("/{taskId}/status")
    @Operation(summary = "크롤링 상태 조회", description = "크롤링 작업의 현재 상태 조회하기")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상태 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                CrawlingStatusResponse.class))),
                @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없음"),
            })
    public ResponseEntity<CrawlingStatusResponse> getStatus(
            @Parameter(description = "작업 ID") @PathVariable UUID taskId,
            @AuthenticationPrincipal UUID userId) {
        CrawlingStatusResponse response = crawlingService.getStatus(taskId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 크롤링 결과 조회
     *
     * @param taskId 작업 ID
     * @param userId 사용자 ID
     * @return 대화 응답
     */
    @GetMapping("/{taskId}/result")
    @Operation(summary = "크롤링 결과 조회", description = "완료된 크롤링 작업의 대화 내용 조회")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "결과 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ConversationResponse.class))),
                @ApiResponse(responseCode = "404", description = "작업 또는 대화를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "크롤링 미완료"),
            })
    public ResponseEntity<ConversationResponse> getResult(
            @Parameter(description = "작업 ID") @PathVariable UUID taskId,
            @AuthenticationPrincipal UUID userId) {
        ConversationResponse response = crawlingService.getResult(taskId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 현재 사용자의 크롤링 작업 목록을 조회
     *
     * @param userId 사용자 ID (인터셉터에서 주입)
     * @return 작업 목록
     */
    @GetMapping("/my/tasks")
    @Operation(summary = "내 작업 목록 조회", description = "현재 로그인한 사용자의 크롤링 작업 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            })
    public ResponseEntity<List<CrawlingTaskSummary>> getMyTasks(
            @AuthenticationPrincipal UUID userId) {
        List<CrawlingTaskSummary> tasks = crawlingService.getTasksByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }
}
