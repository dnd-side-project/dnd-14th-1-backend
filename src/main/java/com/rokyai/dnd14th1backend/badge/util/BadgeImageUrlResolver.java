package com.rokyai.dnd14th1backend.badge.util;

import java.net.URI;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/** 배지 이미지 URL을 API 응답용 공개 URL 형태로 변환한다. */
public final class BadgeImageUrlResolver {

    private BadgeImageUrlResolver() {}

    public static String toPublicUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return imageUrl;
        }
        if (isAbsoluteUrl(imageUrl)) {
            return enforceHttps(imageUrl);
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
            String contextPath =
                    ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return enforceHttps(contextPath);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static String enforceHttps(String url) {
        try {
            URI uri = URI.create(url);
            if (!"http".equalsIgnoreCase(uri.getScheme())) {
                return url;
            }
            return UriComponentsBuilder.fromUri(uri).scheme("https").build().toUriString();
        } catch (IllegalArgumentException e) {
            return url;
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
