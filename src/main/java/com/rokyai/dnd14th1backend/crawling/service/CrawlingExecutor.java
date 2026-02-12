package com.rokyai.dnd14th1backend.crawling.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation;
import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation.CrawledMessage;
import com.rokyai.dnd14th1backend.crawling.crawler.CrawlerRegistry;
import com.rokyai.dnd14th1backend.crawling.crawler.PlatformCrawler;
import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.domain.Conversation;
import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;
import com.rokyai.dnd14th1backend.crawling.domain.Message;
import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.CrawlingTaskRepository;

/** Virtual Thread 기반 크롤링 실행기 */
@Slf4j
@Component
public class CrawlingExecutor {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final CrawlingTaskRepository crawlingTaskRepository;
    private final ConversationRepository conversationRepository;
    private final CrawlerRegistry crawlerRegistry;

    public CrawlingExecutor(
            CrawlingTaskRepository crawlingTaskRepository,
            ConversationRepository conversationRepository,
            CrawlerRegistry crawlerRegistry) {
        this.crawlingTaskRepository = crawlingTaskRepository;
        this.conversationRepository = conversationRepository;
        this.crawlerRegistry = crawlerRegistry;
    }

    /**
     * 크롤링 작업을 비동기로 실행
     *
     * @param task 크롤링 작업 (ID와 URL 정보만 사용)
     * @return 완료된 대화 (CompletableFuture)
     */
    public CompletableFuture<Conversation> executeAsync(CrawlingTask task) {
        UUID taskId = task.getId();
        String sourceUrl = task.getSourceUrl();

        return CompletableFuture.supplyAsync(() -> executeCrawling(taskId, sourceUrl), executor);
    }

    /**
     * 크롤링 작업이 완료되었는지 확인
     *
     * @param task 크롤링 작업
     * @return 완료 여부
     */
    public boolean isCompleted(CrawlingTask task) {
        return task.getStatus() == CrawlingStatus.COMPLETED
                || task.getStatus() == CrawlingStatus.FAILED;
    }

    /**
     * 크롤링실행
     *
     * @param taskId 작업 ID
     * @param sourceUrl 소스 URL
     * @return 생성된 대화
     */
    @Transactional
    protected Conversation executeCrawling(UUID taskId, String sourceUrl) {
        CrawlingTask task =
                crawlingTaskRepository
                        .findById(taskId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.TASK_NOT_FOUND));

        try {
            task.markInProgress();
            crawlingTaskRepository.save(task);

            Conversation conversation = performCrawling(task, sourceUrl);

            task.markCompleted();
            crawlingTaskRepository.save(task);

            return conversation;
        } catch (CrawlingException e) {
            log.error("크롤링 실패: taskId={}, error={}", taskId, e.getMessage());
            task.markFailed(e.getMessage());
            crawlingTaskRepository.save(task);
            throw e;
        } catch (Exception e) {
            log.error("크롤링 실패: taskId={}, error={}", taskId, e.getMessage(), e);
            task.markFailed(e.getMessage());
            crawlingTaskRepository.save(task);
            throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, e.getMessage(), e);
        }
    }

    /**
     * 크롤링 수행
     *
     * @param task 크롤링 작업
     * @param url 소스 URL
     * @return 생성된 대화
     */
    private Conversation performCrawling(CrawlingTask task, String url) {
        // 플랫폼에 적합한 크롤러 가져오기
        PlatformCrawler crawler =
                crawlerRegistry
                        .findCrawler(url)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.UNSUPPORTED_PLATFORM,
                                                "지원하지 않는 URL 형식입니다: " + url));

        log.info(
                "크롤링 시작: taskId={}, platform={}, url={}",
                task.getId(),
                crawler.getSupportedPlatform(),
                url);

        // 크롤링 수행
        CrawledConversation crawledData = crawler.crawl(url);

        // 대화 엔티티 생성
        Conversation conversation =
                Conversation.builder()
                        .crawlingTask(task)
                        .userId(task.getUserId())
                        .title(crawledData.title())
                        .sourceUrl(task.getSourceUrl())
                        .platform(crawledData.platform())
                        .build();

        // 메시지 엔티티 생성 및 추가
        for (CrawledMessage crawledMessage : crawledData.messages()) {
            Message message =
                    Message.create(
                            conversation,
                            crawledMessage.role(),
                            crawledMessage.content(),
                            crawledMessage.sequence());
            conversation.addMessage(message);
        }

        // 메시지를 Chat(질의/응답 쌍)으로 묶어 생성
        createChats(conversation, crawledData.messages());

        Conversation saved = conversationRepository.save(conversation);
        log.info(
                "크롤링 완료: taskId={}, conversationId={}, messageCount={}",
                task.getId(),
                saved.getId(),
                saved.getMessages().size());

        return saved;
    }

    /**
     * 메시지 질의응답을 한 쌍으로 묶어 Chat 생성
     *
     * @param conversation 대화 세션
     * @param crawledMessages 크롤링된 메시지 목록
     */
    private void createChats(
            Conversation conversation, java.util.List<CrawledMessage> crawledMessages) {
        int chatSequence = 1;
        int i = 0;

        while (i < crawledMessages.size()) {
            CrawledMessage current = crawledMessages.get(i);

            if (current.role() == MessageRole.USER) {
                String userContent = current.content();
                String assistantContent = null;

                // 다음 메시지가 ASSISTANT이면 쌍으로 묶음
                if (i + 1 < crawledMessages.size()
                        && crawledMessages.get(i + 1).role() == MessageRole.ASSISTANT) {
                    assistantContent = crawledMessages.get(i + 1).content();
                    i += 2;
                } else {
                    i++;
                }

                Chat chat =
                        Chat.create(conversation, userContent, assistantContent, chatSequence++);
                conversation.addChat(chat);
            } else {
                // ASSISTANT만 단독으로 있는 경우 건너뜀
                i++;
            }
        }
    }
}
