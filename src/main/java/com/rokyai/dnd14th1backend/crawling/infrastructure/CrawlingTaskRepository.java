package com.rokyai.dnd14th1backend.crawling.infrastructure;

import java.util.List;
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
     * 모든 크롤링 작업 목록을 조회 (관리자용)
     *
     * @return 크롤링 작업 목록 (최신순)
     */
    @Query("SELECT t FROM CrawlingTask t ORDER BY t.createdAt DESC")
    List<CrawlingTask> findAllOrderByCreatedAtDesc();
}
