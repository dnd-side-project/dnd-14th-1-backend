package com.rokyai.dnd14th1backend.crawling.crawler;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 입력받은 공유 URL의 플랫폼 판별 및 선택하는 registry */
@Component
public class CrawlerRegistry {

    private final List<PlatformCrawler> crawlers;

    public CrawlerRegistry(List<PlatformCrawler> crawlers) {
        this.crawlers = crawlers;
    }

    /**
     * URL을 처리할 수 있는 크롤러 찾기
     *
     * @param url 대상 URL
     * @return 처리 가능한 크롤러 (Optional)
     */
    public Optional<PlatformCrawler> findCrawler(String url) {
        return crawlers.stream().filter(crawler -> crawler.canHandle(url)).findFirst();
    }

    /**
     * URL에서 플랫폼을 감지
     *
     * @param url 대상 URL
     * @return 플랫폼
     */
    public Platform detectPlatform(String url) {
        return findCrawler(url).map(PlatformCrawler::getSupportedPlatform).orElse(Platform.OTHER);
    }

    /**
     * 해당 URL이 지원되는지 확인함
     *
     * @param url 대상 URL
     * @return 지원 여부
     */
    public boolean isSupported(String url) {
        return findCrawler(url).isPresent();
    }
}
