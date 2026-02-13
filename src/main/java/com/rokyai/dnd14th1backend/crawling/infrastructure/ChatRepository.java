package com.rokyai.dnd14th1backend.crawling.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.crawling.domain.Chat;

/** Chat 리포지토리 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * 대화 ID로 Chat 목록 조회 (순서 오름차순)
     *
     * @param conversationId 대화 ID
     * @return Chat 목록
     */
    @Query("SELECT c FROM Chat c WHERE c.conversation.id = :conversationId ORDER BY c.sequence ASC")
    List<Chat> findByConversationIdOrderBySequenceAsc(@Param("conversationId") UUID conversationId);

    /**
     * 대화 ID와 Chat ID로 Chat 조회
     *
     * @param chatId Chat ID
     * @param conversationId 대화 ID
     * @return Chat (Optional)
     */
    @Query("SELECT c FROM Chat c WHERE c.id = :chatId AND c.conversation.id = :conversationId")
    Optional<Chat> findByIdAndConversationId(
            @Param("chatId") UUID chatId, @Param("conversationId") UUID conversationId);

    /**
     * 대화 내 최대 sequence 조회
     *
     * @param conversationId 대화 ID
     * @return 최대 sequence (없으면 0)
     */
    @Query(
            "SELECT COALESCE(MAX(c.sequence), 0) FROM Chat c WHERE c.conversation.id = :conversationId")
    int findMaxSequenceByConversationId(@Param("conversationId") UUID conversationId);
}
