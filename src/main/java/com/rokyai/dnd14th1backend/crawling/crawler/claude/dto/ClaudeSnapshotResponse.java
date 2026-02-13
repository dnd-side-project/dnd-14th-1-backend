package com.rokyai.dnd14th1backend.crawling.crawler.claude.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Claude 스냅샷 API 응답 DTO */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaudeSnapshotResponse(
        @JsonProperty("uuid") String uuid,
        @JsonProperty("snapshot_name") String snapshotName,
        @JsonProperty("chat_messages") List<ClaudeChatMessage> chatMessages,
        @JsonProperty("is_public") boolean isPublic) {}
