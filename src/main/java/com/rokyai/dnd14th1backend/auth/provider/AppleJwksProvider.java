package com.rokyai.dnd14th1backend.auth.provider;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.auth.exception.InvalidIdTokenException;

/**
 * Apple의 JWKS(JSON Web Key Set)를 관리하고 공개 키를 제공합니다. WebClient를 사용하여 Apple의 JWKS 엔드포인트에서 공개 키를
 * 다운로드하고 캐싱합니다.
 */
@Component
public class AppleJwksProvider {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile long lastRefreshTime = 0;
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24시간

    public AppleJwksProvider() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "DndBackend/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 토큰의 kid(Key ID)로 Apple의 공개 키를 조회합니다.
     *
     * @param keyId 토큰 헤더의 kid 값
     * @return Apple의 공개 키
     * @throws InvalidIdTokenException JWKS 조회 실패 또는 키를 찾을 수 없는 경우
     */
    public PublicKey getPublicKey(String keyId) {
        // 캐시된 키가 있고 TTL이 유효하면 반환
        if (keyCache.containsKey(keyId) && !isCacheExpired()) {
            return keyCache.get(keyId);
        }

        // JWKS 새로 가져오기
        refreshKeys();

        PublicKey key = keyCache.get(keyId);
        if (key == null) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN,
                    "Apple JWKS에서 kid=" + keyId + "에 해당하는 키를 찾을 수 없습니다");
        }
        return key;
    }

    private boolean isCacheExpired() {
        return System.currentTimeMillis() - lastRefreshTime > CACHE_TTL_MS;
    }

    private synchronized void refreshKeys() {
        // 다른 스레드가 이미 갱신했는지 확인
        if (!isCacheExpired() && !keyCache.isEmpty()) {
            return;
        }

        try {
            JwksResponse jwksResponse = webClient.get()
                    .uri(APPLE_JWKS_URL)
                    .retrieve()
                    .bodyToMono(JwksResponse.class)
                    .block(TIMEOUT);

            if (jwksResponse == null || jwksResponse.keys == null) {
                throw new InvalidIdTokenException(
                        AuthStatus.INVALID_ID_TOKEN, "Apple JWKS 응답이 비어있습니다");
            }

            keyCache.clear();
            for (JwkKey jwk : jwksResponse.keys) {
                try {
                    PublicKey publicKey = createPublicKey(jwk);
                    keyCache.put(jwk.kid, publicKey);
                } catch (Exception exception) {
                    // 개별 키 파싱 실패는 무시하고 계속 진행
                }
            }

            lastRefreshTime = System.currentTimeMillis();

            if (keyCache.isEmpty()) {
                throw new InvalidIdTokenException(
                        AuthStatus.INVALID_ID_TOKEN, "Apple JWKS에서 유효한 키를 찾을 수 없습니다");
            }
        } catch (InvalidIdTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN,
                    "Apple JWKS 조회 실패: " + exception.getMessage(),
                    exception);
        }
    }

    private PublicKey createPublicKey(JwkKey jwk) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(jwk.n);
        byte[] eBytes = Base64.getUrlDecoder().decode(jwk.e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JwksResponse {
        @JsonProperty("keys")
        List<JwkKey> keys;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JwkKey {
        @JsonProperty("kty")
        String kty;

        @JsonProperty("kid")
        String kid;

        @JsonProperty("use")
        String use;

        @JsonProperty("alg")
        String alg;

        @JsonProperty("n")
        String n;

        @JsonProperty("e")
        String e;
    }
}
