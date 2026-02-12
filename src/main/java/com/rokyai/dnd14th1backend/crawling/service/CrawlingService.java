package com.rokyai.dnd14th1backend.crawling.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.crawling.crawler.CrawlerRegistry;
import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.domain.Conversation;
import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;
import com.rokyai.dnd14th1backend.crawling.dto.ChatListResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ChatResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ConversationResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingStatusResponse;
import com.rokyai.dnd14th1backend.crawling.dto.CrawlingTaskSummary;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.CrawlingTaskRepository;

/** 크롤링 서비스 */
@Slf4j
@Service
public class CrawlingService {

    private static final long TIMEOUT_SECONDS = 2;

    private final CrawlingTaskRepository crawlingTaskRepository;
    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final CrawlingExecutor crawlingExecutor;
    private final CrawlerRegistry crawlerRegistry;

    public CrawlingService(
            CrawlingTaskRepository crawlingTaskRepository,
            ConversationRepository conversationRepository,
            ChatRepository chatRepository,
            CrawlingExecutor crawlingExecutor,
            CrawlerRegistry crawlerRegistry) {
        this.crawlingTaskRepository = crawlingTaskRepository;
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
        this.crawlingExecutor = crawlingExecutor;
        this.crawlerRegistry = crawlerRegistry;
    }

    /**
     * 크롤링을 요청합니다. 2초 이내 완료 시 결과 반환, 초과 시 taskId 반환
     *
     * @param url 크롤링 대상 URL
     * @param userId 사용자 ID (nullable)
     * @return 크롤링 응답
     */
    public CrawlingResponse requestCrawling(String url, UUID userId) {
        if (url == null || url.isBlank()) {
            throw new CrawlingException(CrawlingErrorStatus.INVALID_URL);
        }

        Platform platform = crawlerRegistry.detectPlatform(url);
        if (platform == Platform.OTHER && !crawlerRegistry.isSupported(url)) {
            throw new CrawlingException(
                    CrawlingErrorStatus.UNSUPPORTED_PLATFORM, "지원하지 않는 URL 형식입니다: " + url);
        }

        CrawlingTask task = CrawlingTask.create(url, platform, userId);
        crawlingTaskRepository.save(task);

        CompletableFuture<Conversation> future = crawlingExecutor.executeAsync(task);

        try {
            // 2초 대기
            Conversation conversation = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return CrawlingResponse.completed(
                    task.getId(), ConversationResponse.from(conversation));
        } catch (TimeoutException e) {
            // 2초 초과 → 백그라운드에서 계속 진행, taskId 반환
            log.info("크롤링 타임아웃, 백그라운드 진행: taskId={}", task.getId());
            return CrawlingResponse.inProgress(task.getId());
        } catch (Exception e) {
            log.error("크롤링 요청 중 오류 발생: {}", e.getMessage(), e);
            throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, e.getMessage());
        }
    }

    /**
     * 크롤링 작업 상태 조회
     *
     * @param taskId 작업 ID
     * @return 상태 응답
     */
    @Transactional(readOnly = true)
    public CrawlingStatusResponse getStatus(UUID taskId) {
        CrawlingTask task =
                crawlingTaskRepository
                        .findById(taskId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.TASK_NOT_FOUND));

        return CrawlingStatusResponse.from(task);
    }

    /**
     * 크롤링 결과조회
     *
     * @param taskId 작업 ID
     * @return 대화 응답
     */
    @Transactional(readOnly = true)
    public ConversationResponse getResult(UUID taskId) {
        CrawlingTask task =
                crawlingTaskRepository
                        .findById(taskId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.TASK_NOT_FOUND));

        if (!crawlingExecutor.isCompleted(task)) {
            throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "크롤링이 아직 완료되지 않았습니다");
        }

        Conversation conversation =
                conversationRepository
                        .findByCrawlingTaskIdWithMessages(taskId)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.CONVERSATION_NOT_FOUND));

        return ConversationResponse.from(conversation);
    }

    /**
     * 모든 크롤링 작업 목록 조회 (관리자용)
     *
     * @return 작업 목록
     */
    @Transactional(readOnly = true)
    public List<CrawlingTaskSummary> getAllTasks() {
        return crawlingTaskRepository.findAllOrderByCreatedAtDesc().stream()
                .map(CrawlingTaskSummary::from)
                .toList();
    }

    /**
     * 사용자의 크롤링 작업 목록 조회
     *
     * @param userId 사용자 ID
     * @return 작업 목록
     */
    @Transactional(readOnly = true)
    public List<CrawlingTaskSummary> getTasksByUser(UUID userId) {
        return crawlingTaskRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CrawlingTaskSummary::from)
                .toList();
    }

    /**
     * Chat 목록 조회
     *
     * @param conversationId 대화 ID
     * @return Chat 목록 응답
     */
    @Transactional(readOnly = true)
    public ChatListResponse getChats(UUID conversationId) {
        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.CONVERSATION_NOT_FOUND));

        List<ChatResponse> chatResponses =
                chatRepository.findByConversationIdOrderBySequenceAsc(conversationId).stream()
                        .map(ChatResponse::from)
                        .toList();

        return new ChatListResponse(conversation.getId(), conversation.getTitle(), chatResponses);
    }

    /**
     * Chat 단일 조회
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     * @return Chat 응답
     */
    @Transactional(readOnly = true)
    public ChatResponse getChat(UUID conversationId, UUID chatId) {
        Chat chat =
                chatRepository
                        .findByIdAndConversationId(chatId, conversationId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.CHAT_NOT_FOUND));

        return ChatResponse.from(chat);
    }

    /**
     * Chat 삭제
     *
     * @param conversationId 대화 ID
     * @param chatId Chat ID
     */
    @Transactional
    public void deleteChat(UUID conversationId, UUID chatId) {
        Chat chat =
                chatRepository
                        .findByIdAndConversationId(chatId, conversationId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.CHAT_NOT_FOUND));

        chatRepository.delete(chat);
    }
}
