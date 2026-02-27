package com.rokyai.dnd14th1backend.badge.util;

import java.net.URI;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** 배지 이미지 URL을 API 응답용 공개 URL 형태로 변환한다. */
public final class BadgeImageUrlResolver {

    private BadgeImageUrlResolver() {}

    public static String toPublicUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || isAbsoluteUrl(imageUrl)) {
            return imageUrl;
        }

        String contextPath = currentContextPath();
        if (!StringUtils.hasText(contextPath)) {
            return imageUrl;
        }

        String normalizedPath = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return contextPath + normalizedPath;
    }

    private static String currentContextPath() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static boolean isAbsoluteUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            return uri.isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
