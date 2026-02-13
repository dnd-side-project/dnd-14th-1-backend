package com.rokyai.dnd14th1backend.users.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Chat 최적화 요청 */
public record OptimizeChatRequest(@Min(1) @Max(4096) int tokenSaving) {}
