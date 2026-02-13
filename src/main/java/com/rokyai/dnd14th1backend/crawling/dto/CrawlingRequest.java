package com.rokyai.dnd14th1backend.crawling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 크롤링 요청 DTO */
@Schema(description = "크롤링 요청")
public record CrawlingRequest(
        @Schema(description = "크롤링 대상 URL", example = "https://chatgpt.com/share/xxx")
                @NotBlank(message = "URL은 필수입니다.")
                @Size(max = 2048, message = "URL은 2048자를 초과할 수 없습니다.")
                String url) {}
