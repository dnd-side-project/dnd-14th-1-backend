package com.rokyai.dnd14th1backend.crawling.crawler.claude.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Claude 채팅 메시지 DTO */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaudeChatMessage(
        @JsonProperty("uuid") String uuid,
        @JsonProperty("sender") String sender,
        @JsonProperty("index") int index,
        @JsonProperty("content") List<ClaudeMessageContent> content) {}
