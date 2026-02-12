package com.rokyai.dnd14th1backend.crawling.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.domain.Conversation;
import com.rokyai.dnd14th1backend.crawling.dto.ChatListResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ChatResponse;
import com.rokyai.dnd14th1backend.crawling.dto.ConversationResponse;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;

/** Conversation/Chat CRUD 서비스 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;

    public ConversationService(
            ConversationRepository conversationRepository, ChatRepository chatRepository) {
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
    }

    /**
     * Conversation 생성
     *
     * @param userId 사용자 ID
     * @param title 대화 제목
     * @return 대화 응답
     */
    @Transactional
    public ConversationResponse createConversation(UUID userId, String title) {
        Conversation conversation = Conversation.createByUser(userId, title);
        conversationRepository.save(conversation);
        return ConversationResponse.from(conversation);
    }

    /**
     * 사용자의 Conversation 목록 조회
     *
     * @param userId 사용자 ID
     * @return 대화 목록
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    /**
     * Conversation 단건 조회
     *
     * @param conversationId 대화 ID
     * @return 대화 응답
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId) {
        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.CONVERSATION_NOT_FOUND));
        return ConversationResponse.from(conversation);
    }

    /**
     * Conversation 삭제 (cascade로 Chat도 삭제)
     *
     * @param conversationId 대화 ID
     */
    @Transactional
    public void deleteConversation(UUID conversationId) {
        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.CONVERSATION_NOT_FOUND));
        conversationRepository.delete(conversation);
    }

    /**
     * Chat 생성
     *
     * @param conversationId 대화 ID
     * @param userContent 사용자 질의 내용
     * @param assistantContent 응답 내용 (nullable)
     * @return Chat 응답
     */
    @Transactional
    public ChatResponse createChat(
            UUID conversationId, String userContent, String assistantContent) {
        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new CrawlingException(
                                                CrawlingErrorStatus.CONVERSATION_NOT_FOUND));

        int nextSequence = chatRepository.findMaxSequenceByConversationId(conversationId) + 1;
        Chat chat = Chat.create(conversation, userContent, assistantContent, nextSequence);
        chatRepository.save(chat);
        return ChatResponse.from(chat);
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
