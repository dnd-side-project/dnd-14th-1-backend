package com.rokyai.dnd14th1backend.crawling.crawler;

import java.util.List;

import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/**
 * 크롤링된 대화
 *
 * @param title 대화 제목
 * @param platform AI 플랫폼 (gpt, claude 등..)
 * @param messages 메시지 목록
 */
public record CrawledConversation(String title, Platform platform, List<CrawledMessage> messages) {

    /**
     * 크롤링된 메시지
     *
     * @param role 역할 (USER/ASSISTANT)
     * @param content 메시지 내용
     * @param sequence 메시지 순서
     */
    public record CrawledMessage(MessageRole role, String content, int sequence) {}
}
