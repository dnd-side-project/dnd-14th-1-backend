package com.rokyai.dnd14th1backend.badge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class BadgeImageUrlResolverTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toPublicUrl_relativePath_returnsAbsoluteUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String result = BadgeImageUrlResolver.toPublicUrl("/badge/enable/enable_gadian.png");

        assertEquals("https://localhost:8080/badge/enable/enable_gadian.png", result);
    }

    @Test
    void toPublicUrl_absoluteUrl_returnsOriginal() {
        String imageUrl = "https://cdn.example.com/badge/enable.png";

        String result = BadgeImageUrlResolver.toPublicUrl(imageUrl);

        assertEquals(imageUrl, result);
    }

    @Test
    void toPublicUrl_absoluteHttpUrl_returnsHttpsUrl() {
        String imageUrl = "http://cdn.example.com/badge/enable.png";

        String result = BadgeImageUrlResolver.toPublicUrl(imageUrl);

        assertEquals("https://cdn.example.com/badge/enable.png", result);
    }

    @Test
    void toPublicUrl_noRequestContext_returnsOriginal() {
        String imageUrl = "/badge/enable/enable_gadian.png";

        String result = BadgeImageUrlResolver.toPublicUrl(imageUrl);

        assertEquals(imageUrl, result);
    }
}
