package com.rokyai.dnd14th1backend.crawling.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.crawling.domain.Message;

/** 메시지 레포지토리 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {}
