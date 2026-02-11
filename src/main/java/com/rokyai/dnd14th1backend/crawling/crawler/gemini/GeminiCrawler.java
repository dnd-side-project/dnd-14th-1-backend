package com.rokyai.dnd14th1backend.crawling.crawler.gemini;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import com.rokyai.dnd14th1backend.crawling.enums.MessageRole;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;

/** Gemini 대화 크롤러 */
@Slf4j
@Component
public class GeminiCrawler implements PlatformCrawler {

    private static final String GEMINI_BASE = "https://gemini.google.com";
    private static final String BATCHEXECUTE_PATH = "/_/BardChatUi/data/batchexecute";
    private static final String RPC_METHOD = "ujx1Bf";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";

    // cli 업데이트마다 변동성이 많을 것으로 예상됨 ..

    // gemini.google.com/share/{id} 또는 g.co/gemini/share/{id}
    private static final Pattern SHARE_URL_PATTERN =
            Pattern.compile(
                    "https?://(gemini\\.google\\.com/share/|g\\.co/gemini/share/)[a-zA-Z0-9_-]+",
                    Pattern.CASE_INSENSITIVE);

    // share ID
    private static final Pattern SHARE_ID_PATTERN =
            Pattern.compile(".*/share/([a-zA-Z0-9_-]+)");

    // 공유 페이지 HTML에서 cfb2h 추출
    private static final Pattern BL_PARAM_PATTERN =
            Pattern.compile("\"cfb2h\":\"([^\"]+)\"");

    // batchexecute에서 JSON 문자열 추출
    private static final Pattern BATCH_DATA_PATTERN =
            Pattern.compile("\\[\"wrb\\.fr\",\"" + RPC_METHOD + "\",\"(.*)\",null,null,null,\"generic\"\\]");

    // 사용자 메시지 -> [["메시지"],2,null,0,"turnId",0]
    private static final Pattern USER_MESSAGE_PATTERN =
            Pattern.compile(
                    "\\[\\[\"((?:[^\"\\\\]|\\\\.)*)\"\\],\\s*2,\\s*null,\\s*0,\\s*\"[a-f0-9]+\",\\s*0\\]");

    // 어시스턴트 -> ["rc_hexid",["응답 텍스트"],...]
    private static final Pattern RESPONSE_CONTENT_PATTERN =
            Pattern.compile("\\[\"rc_[a-f0-9]+\",\\s*\\[\"((?:[^\"\\\\]|\\\\.)*)\"\\]");

    // 구조화된 응답 파트 패턴: [null,[null,0,"텍스트"]]
    private static final Pattern RESPONSE_PART_PATTERN =
            Pattern.compile("\\[null,\\[null,0,\"((?:[^\"\\\\]|\\\\.)*)\"\\]\\]");

    private final WebClient webClient;

    public GeminiCrawler() {
        this.webClient =
                WebClient.builder()
                        .baseUrl(GEMINI_BASE)
                        .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                        .build();
    }

    @Override
    public Platform getSupportedPlatform() {
        return Platform.GEMINI;
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
        String shareId = extractShareId(url);
        log.info("Gemini 대화 크롤링 시작: shareId={}", shareId);

        String blParam = fetchBlParam(shareId);
        String responseData = callBatchExecute(shareId, blParam);
        return parseConversation(responseData);
    }

    /**
     * URL에서 share ID 추출
     *
     * @param url 공유 URL
     * @return share ID
     */
    private String extractShareId(String url) {
        Matcher matcher = SHARE_ID_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new CrawlingException(
                    CrawlingErrorStatus.INVALID_URL, "유효하지 않은 Gemini 공유 URL입니다.");
        }
        return matcher.group(1);
    }

    /**
     * 공유 페이지에서 bl 파라미터 추출 (batchexecute 호출에 필요)
     *
     * @param shareId 공유 ID
     * @return bl 파라미터 값
     */
    private String fetchBlParam(String shareId) {
        try {
            String html =
                    webClient
                            .get()
                            .uri("/share/" + shareId)
                            .accept(MediaType.TEXT_HTML)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            if (html != null) {
                Matcher matcher = BL_PARAM_PATTERN.matcher(html);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            log.warn("bl 파라미터 추출 실패, 기본값 사용: {}", e.getMessage());
        }

        return "boq_assistant-bard-web-server_20260209.08_p0";
    }

    /**
     * batchexecute API 호출로 대화 데이터 가져오기
     *
     * @param shareId 공유 ID
     * @param blParam bl 파라미터
     * @return batchexecute 응답 원문
     */
    private String callBatchExecute(String shareId, String blParam) {
        String requestBody =
                String.format("[[[\"%s\",\"[null,\\\"%s\\\"]\",null,\"generic\"]]]", RPC_METHOD, shareId);

        try {
            String response =
                    webClient
                            .post()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(BATCHEXECUTE_PATH)
                                            .queryParam("bl", blParam)
                                            .queryParam("hl", "ko")
                                            .queryParam("_reqid", "0")
                                            .queryParam("rt", "c")
                                            .queryParam("source-path", "/share/" + shareId)
                                            .build())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header(HttpHeaders.ORIGIN, GEMINI_BASE)
                            .header(HttpHeaders.REFERER, GEMINI_BASE + "/share/" + shareId)
                            .bodyValue("f.req=" + URLEncoder.encode(requestBody, StandardCharsets.UTF_8))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            if (response == null || response.isBlank()) {
                throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "batchexecute 응답이 비어있습니다.");
            }

            return response;

        } catch (WebClientResponseException.NotFound e) {
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "대화를 찾을 수 없습니다: " + shareId);
        } catch (WebClientResponseException.Forbidden e) {
            throw new CrawlingException(CrawlingErrorStatus.CRAWLING_FAILED, "접근이 거부되었습니다.");
        } catch (WebClientResponseException e) {
            log.error("Gemini batchexecute 호출 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * batchexecute 응답에서 대화 parsing
     *
     * @param rawResponse batchexecute 응답 원문
     * @return 크롤링된 대화
     */
    private CrawledConversation parseConversation(String rawResponse) {
        // batchexecute 응답에서 데이터 JSON 문자열 추출
        String data = extractDataFromResponse(rawResponse);

        // 이스케이프 해제 (데이터가 JSON 문자열로 감싸져 있음)
        String normalized = data.replace("\\\"", "\"")
                .replace("\\\\n", "\n")
                .replace("\\\\", "\\");

        List<CrawledMessage> messages = extractMessages(normalized);

        if (messages.isEmpty()) {
            log.warn("batchexecute 응답에서 메시지를 추출할 수 없습니다.");
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "대화 내용을 찾을 수 없습니다");
        }

        log.info("Gemini 크롤링 완료: messageCount={}", messages.size());
        return new CrawledConversation("Gemini 대화", Platform.GEMINI, messages);
    }

    /**
     * batchexecute 원문 응답에서 데이터 JSON 문자열을 추출
     *
     * @param rawResponse 원문 응답
     * @return 데이터 JSON 문자열
     */
    private String extractDataFromResponse(String rawResponse) {
        // 응답 형식: )]}\n\n{length}\n[["wrb.fr","ujx1Bf","DATA",null,null,null,"generic"]]
        // DATA 부분을 추출해야 함

        // wrb.fr 라인 찾기
        int wrbIndex = rawResponse.indexOf("[\"wrb.fr\"");
        if (wrbIndex == -1) {
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "batchexecute 응답 형식이 올바르지 않습니다.");
        }

        String wrbLine = rawResponse.substring(wrbIndex);

        // RPC 메서드 이름 뒤의 데이터 문자열 추출 (,"ujx1Bf","DATA",null)
        String methodPrefix = ",\"" + RPC_METHOD + "\",\"";
        int dataStart = wrbLine.indexOf(methodPrefix);
        if (dataStart == -1) {
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "batchexecute 응답에서 데이터를 찾을 수 없습니다.");
        }

        dataStart += methodPrefix.length();

        // 데이터 문자열의 끝 찾기 (,null,null,null,"generic")
        String dataSuffix = "\",null,null,null,\"generic\"";
        int dataEnd = wrbLine.indexOf(dataSuffix, dataStart);
        if (dataEnd == -1) {
            throw new CrawlingException(
                    CrawlingErrorStatus.CRAWLING_FAILED, "batchexecute 응답에서 데이터 끝을 찾을 수 없습니다.");
        }

        return wrbLine.substring(dataStart, dataEnd);
    }

    /**
     * 정규화된 데이터에서 사용자/어시스턴트 메시지 추출
     *
     * @param data 정규화된 데이터
     * @return 추출된 메시지 목록
     */
    private List<CrawledMessage> extractMessages(String data) {
        List<CrawledMessage> messages = new ArrayList<>();

        // 사용자 메시지 위치 찾기
        Matcher userMatcher = USER_MESSAGE_PATTERN.matcher(data);
        List<PositionedText> userMessages = new ArrayList<>();
        while (userMatcher.find()) {
            String text = unescapeJson(userMatcher.group(1));
            if (text != null && !text.isBlank()) {
                userMessages.add(new PositionedText(text, userMatcher.start(), userMatcher.end()));
            }
        }

        if (userMessages.isEmpty()) {
            return List.of();
        }

        // 각 사용자 메시지 다음의 어시스턴트 응답 찾기
        for (int i = 0; i < userMessages.size(); i++) {
            PositionedText userMsg = userMessages.get(i);
            messages.add(new CrawledMessage(MessageRole.USER, userMsg.text, messages.size() + 1));

            int searchStart = userMsg.end;
            int searchEnd =
                    (i + 1 < userMessages.size())
                            ? userMessages.get(i + 1).start
                            : data.length();

            String segment = data.substring(searchStart, searchEnd);
            String responseText = extractResponseFromSegment(segment);

            if (responseText != null && !responseText.isBlank()) {
                messages.add(
                        new CrawledMessage(
                                MessageRole.ASSISTANT, responseText, messages.size() + 1));
            }
        }

        return messages;
    }

    /**
     * 어시스턴트 응답 텍스트 추출
     *
     * @param segment 검색 세그먼트
     * @return 응답 텍스트
     */
    private String extractResponseFromSegment(String segment) {
        // 방법 1: rc_ 패턴에서 전체 응답 텍스트 추출
        Matcher rcMatcher = RESPONSE_CONTENT_PATTERN.matcher(segment);
        if (rcMatcher.find()) {
            return unescapeJson(rcMatcher.group(1));
        }

        // 방법 2: 구조화된 파트에서 텍스트 조합
        Matcher partMatcher = RESPONSE_PART_PATTERN.matcher(segment);
        StringBuilder sb = new StringBuilder();
        while (partMatcher.find()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(unescapeJson(partMatcher.group(1)));
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * JSON 이스케이프 해제
     *
     * @param text 이스케이프된 텍스트
     * @return 이스케이프 해제된 텍스트
     */
    private String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private record PositionedText(String text, int start, int end) {}
}
