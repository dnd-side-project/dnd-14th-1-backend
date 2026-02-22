package com.rokyai.dnd14th1backend.promptanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 프롬프트 개선 제안 요청 */
@Schema(description = "프롬프트 개선 제안")
public record PromptSuggestionRequest(
        @Schema(description = "사용자 입력 중 모호하거나 개선이 필요한 원본 텍스트 부분") @NotBlank String originalPart,
        @Schema(description = "LLM이 문맥을 더 잘 이해할 수 있도록 구체화하고 명확하게 수정한 텍스트") @NotBlank
                String improvedPart,
        @Schema(description = "왜 이렇게 수정했는지에 대한 논리적이고 구체적인 이유") @NotBlank String reason) {}
