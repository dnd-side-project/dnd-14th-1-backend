package com.rokyai.dnd14th1backend.auth.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.rokyai.dnd14th1backend.auth.dto.AppleIdTokenPayload;

class AppleIdTokenVerifierTest {

    @Test
    void shouldParsePayloadWithUnknownClaimsAndArrayAudience() {
        AppleIdTokenVerifier verifier =
                new AppleIdTokenVerifier(new ObjectMapper(), new NoOpAppleIdTokenValidator());

        String token =
                jwt(
                        """
                        {
                          "sub": "apple-user-123",
                          "email": "user@example.com",
                          "email_verified": "true",
                          "aud": ["com.photocard.master"],
                          "iss": "https://appleid.apple.com",
                          "iat": 1730000000,
                          "exp": 1730003600,
                          "is_private_email": "true",
                          "real_user_status": 1,
                          "nonce": "abc123"
                        }
                        """);

        AppleIdTokenPayload payload = verifier.verifyAndExtractPayload(token);

        assertEquals("apple-user-123", payload.getSubject());
        assertEquals("user@example.com", payload.getEmail());
        assertTrue(payload.getEmailVerified());
        assertEquals("com.photocard.master", payload.getAudience());
    }

    @Test
    void shouldParseBooleanClaimsWhenBooleanTypeProvided() {
        AppleIdTokenVerifier verifier =
                new AppleIdTokenVerifier(new ObjectMapper(), new NoOpAppleIdTokenValidator());

        String token =
                jwt(
                        """
                        {
                          "sub": "apple-user-456",
                          "email_verified": false,
                          "nonce_supported": true,
                          "aud": "com.photocard.master"
                        }
                        """);

        AppleIdTokenPayload payload = verifier.verifyAndExtractPayload(token);

        assertFalse(payload.getEmailVerified());
        assertTrue(payload.getNonceSupported());
        assertEquals("com.photocard.master", payload.getAudience());
    }

    private String jwt(String payloadJson) {
        String header = base64Url("{\"alg\":\"RS256\",\"kid\":\"test-key\"}");
        String payload = base64Url(payloadJson.replaceAll("\\s+", " ").trim());
        return header + "." + payload + ".signature";
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static class NoOpAppleIdTokenValidator extends AppleIdTokenValidator {

        NoOpAppleIdTokenValidator() {
            super(new AppleJwksProvider(), "");
        }

        @Override
        public void validateAndDecode(String idToken) {
            // no-op for payload parsing unit tests
        }
    }
}
