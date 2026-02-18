package com.rokyai.dnd14th1backend.crawling.crawler.claude;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation;
import com.rokyai.dnd14th1backend.crawling.crawler.CrawledConversation.CrawledMessage;
import com.rokyai.dnd14th1backend.crawling.crawler.PlatformCrawler;
import com.rokyai.dnd14th1backend.crawling.crawler.claude.dto.ClaudeChatMessage;
import com.rokyai.dnd14th1backend.crawling.crawler.claude.dto.ClaudeMessageContent;
import com.rokyai.dnd14th1backend.crawling.crawler.claude.dto.ClaudeSnapshotResponse;
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;

/** Claude.ai 크롤러 */
@Slf4j
@Component
public class ClaudeCrawler implements PlatformCrawler {

    private static final String CLAUDE_API_BASE = "https://claude.ai";
    private static final String SNAPSHOT_API_PATH =
            "/api/chat_snapshots/%s?rendering_mode=messages&render_all_tools=true";

    // 공유 URL : claude.ai/share/{uuid}
    private static final Pattern SHARE_URL_PATTERN =
            Pattern.compile("https?://claude\\.ai/share/([a-f0-9\\-]+)", Pattern.CASE_INSENSITIVE);

    private final WebClient webClient;

    public ClaudeCrawler() {
        this.webClient =
                WebClient.builder()
                        .baseUrl(CLAUDE_API_BASE)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(
                                HttpHeaders.USER_AGENT,
                                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                                        + "Chrome/120.0.0.0 Safari/537.36")
                        .build();
    }

    @Override
    public Platform getSupportedPlatform() {
        return Platform.CLAUDE;
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
        String uuid = extractUuid(url);
        log.info("Claude 대화 크롤링 시작: uuid={}", uuid);

        ClaudeSnapshotResponse response = fetchSnapshot(uuid);
        return convertToConversation(response);
    }

    /**
     * URL에서 UUID 추출
     *
     * @param url 공유 URL
     * @return UUID
     */
    private String extractUuid(String url) {
        Matcher matcher = SHARE_URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new CrawlingException(
                    CrawlingErrorStatus.INVALID_URL, "유효하지 않은 Claude 공유 URL입니다.");
        }
        return matcher.group(1);
    }

    /**
     * Claude API에서 스냅샷 가져오기
     *
     * @param uuid 대화 UUID
     * @return 스냅샷 response
     */
    private ClaudeSnapshotResponse fetchSnapshot(String uuid) {
        String apiPath = String.format(SNAPSHOT_API_PATH, uuid);

        try {
            ClaudeSnapshotResponse response =
                    webClient
                            .get()
                            .uri(apiPath)
                            .retrieve()
                            .bodyToMono(ClaudeSnapshotResponse.class)
                            .block(Duration.ofSeconds(30));

            if (response == null) {
                throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "응답이 비어있습니다.");
            }

            if (!response.isPublic()) {
                throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "공개되지 않은 대화입니다.");
            }

            return response;
        } catch (WebClientResponseException.NotFound e) {
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "대화를 찾을 수 없습니다: " + uuid);
        } catch (WebClientResponseException.Forbidden e) {
            throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "접근이 거부되었습니다.");
        } catch (WebClientResponseException e) {
            log.error(
                    "Claude API 호출 실패: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Claude 응답을 공통 대화 형식으로 변환
     *
     * @param response Claude 스냅샷 응답
     * @return 변환된 대화
     */
    private CrawledConversation convertToConversation(ClaudeSnapshotResponse response) {
        List<CrawledMessage> messages = new ArrayList<>();

        for (ClaudeChatMessage chatMessage : response.chatMessages()) {
            MessageRole role = convertRole(chatMessage.sender());
            String content = extractTextContent(chatMessage.content());
            int sequence = chatMessage.index() + 1; // 1-based sequence 대응

            messages.add(new CrawledMessage(role, content, sequence));
        }

        return new CrawledConversation(response.snapshotName(), Platform.CLAUDE, messages);
    }

    /**
     * Claude sender를 MessageRole로 변환
     *
     * @param sender Claude sender (human/assistant) -> (USER/ASSISTANT)
     * @return MessageRole
     */
    private MessageRole convertRole(String sender) {
        return "human".equalsIgnoreCase(sender) ? MessageRole.USER : MessageRole.ASSISTANT;
    }

    /**
     * 콘텐츠 목록에서 텍스트 추출
     *
     * @param contents 콘텐츠 목록
     * @return 합쳐진 텍스트
     */
    private String extractTextContent(List<ClaudeMessageContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ClaudeMessageContent content : contents) {
            if ("text".equals(content.type()) && content.text() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(content.text());
            }
        }
        return sb.toString();
    }
}
