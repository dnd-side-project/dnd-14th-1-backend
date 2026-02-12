package com.rokyai.dnd14th1backend.crawling.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.rokyai.dnd14th1backend.crawling.dto.ChatListResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ChatResponse;
import com.rokyai.dnd14th1backend.crawling.service.CrawlingService;

/** Conversation, Chat 컨트롤러 */
@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "대화", description = "대화 Chat 관리 API")
public class ConversationController {

    private final CrawlingService crawlingService;

    public ConversationController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    /**
     * Chat 목록 조회
     *
     * @param conversationId 대화 ID
     * @return Chat 목록 응답
     */
    @GetMapping("/{conversationId}/chats")
    @Operation(summary = "Chat 목록 조회", description = "하나의 Conversation의 모든 Chat 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ChatListResponse.class))),
                @ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음"),
            })
    public ResponseEntity<ChatListResponse> getChats(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId) {
        ChatListResponse response = crawlingService.getChats(conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat 단일 조회
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     * @return Chat 응답
     */
    @GetMapping("/{conversationId}/chats/{chatId}")
    @Operation(summary = "Chat 단일 조회", description = "특정 Chat의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ChatResponse.class))),
                @ApiResponse(responseCode = "404", description = "Chat을 찾을 수 없음"),
            })
    public ResponseEntity<ChatResponse> getChat(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Parameter(description = "Chat ID") @PathVariable UUID chatId) {
        ChatResponse response = crawlingService.getChat(conversationId, chatId);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat 삭제
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     * @return 204 No Content
     */
    @DeleteMapping("/{conversationId}/chats/{chatId}")
    @Operation(summary = "Chat 삭제", description = "특정 Chat을 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "삭제 성공"),
                @ApiResponse(responseCode = "404", description = "Chat을 찾을 수 없음"),
            })
    public ResponseEntity<Void> deleteChat(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Parameter(description = "Chat ID") @PathVariable UUID chatId) {
        crawlingService.deleteChat(conversationId, chatId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
