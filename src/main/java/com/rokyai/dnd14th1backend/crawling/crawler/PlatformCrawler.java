package com.rokyai.dnd14th1backend.crawling.crawler;

import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 플랫폼별 크롤러 인터페이스 */
public interface PlatformCrawler {

    /**
     * 이 크롤러가 지원하는 플랫폼을 반환
     *
     * @return 지원 플랫폼
     */
    Platform getSupportedPlatform();

    /**
     * 해당 URL을 이 크롤러가 처리할 수 있는지 확인
     *
     * @param url 대상 URL
     * @return 처리 가능 여부
     */
    boolean canHandle(String url);

    /**
     * URL에서 대화 크롤링
     *
     * @param url 크롤링 대상 URL
     * @return 크롤링된 대화
     */
    CrawledConversation crawl(String url);
}
