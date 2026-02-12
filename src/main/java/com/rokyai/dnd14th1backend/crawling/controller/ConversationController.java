package com.rokyai.dnd14th1backend.crawling.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
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

import com.rokyai.dnd14th1backend.crawling.dto.ChatListResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ChatResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ConversationResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CreateChatRequest;
import com.rokyai.dnd14th1backend.crawling.dto.CreateConversationRequest;
import com.rokyai.dnd14th1backend.crawling.service.ConversationService;

/** Conversation, Chat 컨트롤러 */
@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "대화", description = "대화 Chat 관리 API")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Conversation 생성
     *
     * @param request 생성 요청
     * @param userId 사용자 ID
     * @return Conversation 응답
     */
    @PostMapping
    @Operation(summary = "Conversation 생성", description = "새로운 대화를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "생성 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ConversationResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            })
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        ConversationResponse response =
                conversationService.createConversation(userId, request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 Conversation 목록 조회
     *
     * @param userId 사용자 ID
     * @return Conversation 목록
     */
    @GetMapping
    @Operation(summary = "내 Conversation 목록 조회", description = "현재 사용자의 대화 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
            })
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        List<ConversationResponse> responses = conversationService.getConversations(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Conversation 단건 조회
     *
     * @param conversationId 대화 ID
     * @param userId 사용자 ID
     * @return Conversation 응답
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Conversation 단건 조회", description = "특정 대화의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ConversationResponse.class))),
                @ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음"),
            })
    public ResponseEntity<ConversationResponse> getConversation(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        ConversationResponse response = conversationService.getConversation(userId, conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Conversation 삭제
     *
     * @param conversationId 대화 ID
     * @param userId 사용자 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Conversation 삭제", description = "대화를 삭제합니다 (Chat도 함께 삭제)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "삭제 성공"),
                @ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음"),
            })
    public ResponseEntity<Void> deleteConversation(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        conversationService.deleteConversation(userId, conversationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Chat 생성
     *
     * @param conversationId 대화 ID
     * @param request 생성 요청
     * @param userId 사용자 ID
     * @return Chat 응답
     */
    @PostMapping("/{conversationId}/chats")
    @Operation(summary = "Chat 생성", description = "대화에 새로운 Chat(질의/응답 쌍)을 추가합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "생성 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ChatResponse.class))),
                @ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음"),
            })
    public ResponseEntity<ChatResponse> createChat(
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Valid @RequestBody CreateChatRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        ChatResponse response =
                conversationService.createChat(
                        userId, conversationId, request.userContent(), request.assistantContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Chat 목록 조회
     *
     * @param conversationId 대화 ID
     * @param userId 사용자 ID
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
            @Parameter(description = "대화 ID") @PathVariable UUID conversationId,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        ChatListResponse response = conversationService.getChats(userId, conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat 단일 조회
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     * @param userId 사용자 ID
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
            @Parameter(description = "Chat ID") @PathVariable UUID chatId,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        ChatResponse response = conversationService.getChat(userId, conversationId, chatId);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat 삭제
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     * @param userId 사용자 ID
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
            @Parameter(description = "Chat ID") @PathVariable UUID chatId,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        conversationService.deleteChat(userId, conversationId, chatId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
