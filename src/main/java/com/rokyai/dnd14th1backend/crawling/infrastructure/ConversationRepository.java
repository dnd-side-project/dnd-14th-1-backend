package com.rokyai.dnd14th1backend.crawling.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.crawling.domain.Conversation;

/** 대화 세션 레포지토리 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * 크롤링 작업 ID로 대화 조회
     *
     * @param crawlingTaskId 크롤링 작업 ID
     * @return 대화 (Optional)
     */
    @Query(
            "SELECT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.crawlingTask.id = :crawlingTaskId")
    Optional<Conversation> findByCrawlingTaskIdWithMessages(
            @Param("crawlingTaskId") UUID crawlingTaskId);

    /**
     * 사용자 ID로 대화 목록 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 대화 목록
     */
    List<Conversation> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
