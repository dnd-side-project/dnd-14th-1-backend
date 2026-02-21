package com.rokyai.dnd14th1backend.crawling.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;

/** 크롤링 작업 레포지토리 */
@Repository
public interface CrawlingTaskRepository extends JpaRepository<CrawlingTask, UUID> {

    /**
     * 사용자 ID로 크롤링 작업 목록을 조회
     *
     * @param userId 사용자 ID
     * @return 크롤링 작업 목록 (최신순)
     */
    @Query("SELECT t FROM CrawlingTask t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<CrawlingTask> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    /**
     * 작업 ID + 사용자 ID로 크롤링 작업 조회
     *
     * @param id 작업 ID
     * @param userId 사용자 ID
     * @return 크롤링 작업 (Optional)
     */
    Optional<CrawlingTask> findByIdAndUserId(UUID id, UUID userId);

    /**
     * 유저의 누적 크롤링 요청 횟수 조회.
     *
     * @param userId 사용자 ID
     * @return 크롤링 작업 수 (상태 무관)
     */
    @Query("SELECT COUNT(t) FROM CrawlingTask t WHERE t.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);
}
