package com.rokyai.dnd14th1backend.crawling.crawler.chatgpt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation;
import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation.CrawledMessage;
import com.rokyai.dnd14th1backend.crawling.crawler.PlatformCrawler;
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;

/** ChatGPT 크롤러 (Playwright) */
@Slf4j
@Component
public class ChatGptCrawler implements PlatformCrawler, DisposableBean {

    // chatgpt.com/share/{id} 또는 chat.openai.com/share/{id} 패턴
    private static final Pattern SHARE_URL_PATTERN =
            Pattern.compile(
                    "https?://(chatgpt\\.com|chat\\.openai\\.com)/share/[a-zA-Z0-9-]+",
                    Pattern.CASE_INSENSITIVE);

    private volatile Playwright playwright;
    private volatile Browser browser;
    private final Object browserLock = new Object();

    @Override
    public Platform getSupportedPlatform() {
        return Platform.CHATGPT;
    }

    @Override
    public boolean canHandle(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return SHARE_URL_PATTERN.matcher(url).matches();
    }

    @Override
    public CrawledConversation crawl(String url) {
        log.info("ChatGPT 대화 크롤링 시작: url={}", url);

        Browser b = ensureBrowser();

        try (BrowserContext context = b.newContext()) {
            Page page = context.newPage();

            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));

            // 메시지 렌더링 대기
            page.waitForSelector(
                    "[data-message-author-role]",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
            page.waitForTimeout(2000);

            String title = extractTitle(page);
            List<CrawledMessage> messages = extractMessages(page);

            if (messages.isEmpty()) {
                log.warn("메시지를 찾을 수 없습니다. url={}", url);
                throw new CrawlingException(
                        CrawlingErrorStatus.CRAWLING_FAILED, "대화 내용을 찾을 수 없습니다");
            }

            log.info("ChatGPT 크롤링 완료: title={}, messageCount={}", title, messages.size());
            return new CrawledConversation(title, Platform.CHATGPT, messages);

        } catch (CrawlingException e) {
            throw e;
        } catch (PlaywrightException e) {
            log.error("ChatGPT 크롤링 실패: url={}, error={}", url, e.getMessage());
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "페이지 로드 실패: " + e.getMessage());
        }
    }

    /** 페이지에서 제목 추출 */
    private String extractTitle(Page page) {
        String title = page.title();
        if (title != null) {
            title = title.replace(" - ChatGPT", "").replace("ChatGPT - ", "").trim();
        }
        return (title != null && !title.isBlank()) ? title : "ChatGPT 대화";
    }

    /** DOM에서 메시지 추출 */
    private List<CrawledMessage> extractMessages(Page page) {
        List<CrawledMessage> messages = new ArrayList<>();

        List<Locator> elements =
                page.locator("[data-message-author-role]").all();

        for (Locator element : elements) {
            String role = element.getAttribute("data-message-author-role");
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }

            // 마크다운 렌더링된 영역에서 텍스트 추출
            Locator contentArea = element.locator(".markdown, .whitespace-pre-wrap").first();
            String content;
            if (contentArea.count() > 0) {
                content = contentArea.innerText().trim();
            } else {
                content = element.innerText().trim();
            }

            if (content.isBlank()) {
                continue;
            }

            MessageRole messageRole =
                    "user".equals(role) ? MessageRole.USER : MessageRole.ASSISTANT;
            messages.add(new CrawledMessage(messageRole, content, messages.size() + 1));
        }

        return messages;
    }

    /** Playwright Browser 인스턴스 초기화 */
    private Browser ensureBrowser() {
        if (browser == null) {
            synchronized (browserLock) {
                if (browser == null) {
                    log.info("Playwright 브라우저 초기화 중...");
                    playwright = Playwright.create();
                    browser =
                            playwright
                                    .chromium()
                                    .launch(new BrowserType.LaunchOptions().setHeadless(true));
                    log.info("Playwright 브라우저 초기화 완료");
                }
            }
        }
        return browser;
    }

    @Override
    public void destroy() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        log.info("Playwright 리소스 정리 완료");
    }
}
