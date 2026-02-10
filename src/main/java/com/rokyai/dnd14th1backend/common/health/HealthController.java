package com.rokyai.dnd14th1backend.common.health;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rokyai.dnd14th1backend.common.response.SkipApiResponseWrapper;

@RestController
public class HealthController {

    @GetMapping("/health")
    @SkipApiResponseWrapper
    public Map<String, Object> health() {
        return Map.of("status", "UP", "timestamp", Instant.now().toString());
    }

    @GetMapping("/health/error")
    @SkipApiResponseWrapper
    public void testError() {
        throw new RuntimeException("Sentry 테스트용");
    }
}
