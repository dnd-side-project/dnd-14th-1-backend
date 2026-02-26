package com.rokyai.dnd14th1backend.auth.provider;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.rokyai.dnd14th1backend.auth.dto.AppleIdTokenPayload;
import com.rokyai.dnd14th1backend.auth.exception.InvalidIdTokenException;

/**
 * Apple ID Token을 검증하고 파싱하는 역할을 담당합니다. 클라이언트에서 받은 ID Token을 Apple의 공개 키로 서명 검증한 후, 페이로드를 파싱하여
 * AppleIdTokenPayload로 변환합니다.
 */
@Component
public class AppleIdTokenVerifier {

    private static final int JWT_PARTS = 3;
    private final ObjectMapper objectMapper;
    private final AppleIdTokenValidator appleIdTokenValidator;

    public AppleIdTokenVerifier(
            ObjectMapper objectMapper, AppleIdTokenValidator appleIdTokenValidator) {
        this.objectMapper = objectMapper;
        this.appleIdTokenValidator = appleIdTokenValidator;
    }

    /**
     * Apple ID Token을 검증하고 페이로드를 추출합니다. 포맷 검증 → 서명 검증 → 클레임 검증 → 페이로드 파싱 순서로 수행됩니다.
     *
     * @param idToken Apple ID Token
     * @return 검증된 토큰 페이로드
     * @throws InvalidIdTokenException 토큰이 유효하지 않으면 발생
     */
    public AppleIdTokenPayload verifyAndExtractPayload(String idToken) {
        try {
            validateTokenFormat(idToken);
            appleIdTokenValidator.validateAndDecode(idToken);
            return extractPayload(idToken);
        } catch (InvalidIdTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidIdTokenException("ID Token 파싱 실패", exception);
        }
    }

    /**
     * ID Token 포맷을 검증합니다 (JWT 형식: header.payload.signature).
     *
     * @param idToken Apple ID Token
     * @throws InvalidIdTokenException 포맷이 유효하지 않으면 발생
     */
    private void validateTokenFormat(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length != JWT_PARTS) {
            throw new InvalidIdTokenException("ID Token 형식이 올바르지 않습니다");
        }
    }

    /**
     * ID Token의 페이로드 부분을 디코딩하고 AppleIdTokenPayload로 변환합니다.
     *
     * @param idToken Apple ID Token
     * @return 디코딩된 페이로드
     * @throws Exception 디코딩 또는 변환 실패 시 발생
     */
    private AppleIdTokenPayload extractPayload(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        String decodedPayload =
                new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonNode payloadNode = objectMapper.readTree(decodedPayload);

        return AppleIdTokenPayload.builder()
                .subject(readText(payloadNode, "sub"))
                .email(readText(payloadNode, "email"))
                .emailVerified(readBoolean(payloadNode, "email_verified"))
                .audience(readAudience(payloadNode.get("aud")))
                .issuer(readText(payloadNode, "iss"))
                .issuedAt(readLong(payloadNode, "iat"))
                .expiresAt(readLong(payloadNode, "exp"))
                .codeHash(readText(payloadNode, "c_hash"))
                .authTime(readLong(payloadNode, "auth_time"))
                .nonceSupported(readBoolean(payloadNode, "nonce_supported"))
                .build();
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long readLong(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.longValue();
        }
        if (value.isTextual()) {
            try {
                return Long.parseLong(value.asText());
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }

    private Boolean readBoolean(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isTextual()) {
            String text = value.asText();
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return null;
    }

    private String readAudience(JsonNode audienceNode) {
        if (audienceNode == null || audienceNode.isNull()) {
            return null;
        }
        if (audienceNode.isTextual()) {
            return audienceNode.asText();
        }
        if (audienceNode.isArray() && !audienceNode.isEmpty()) {
            JsonNode first = audienceNode.get(0);
            return first == null || first.isNull() ? null : first.asText();
        }
        return null;
    }
}
