package com.rokyai.dnd14th1backend.crawling.crawler.claude.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Claude 메시지 콘텐츠 DTO */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaudeMessageContent(
        @JsonProperty("type") String type, @JsonProperty("text") String text) {}
