package com.rokyai.dnd14th1backend.auth.provider;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/** JWT 토큰 생성 및 검증을 담당합니다. */
@Slf4j
@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final Long expirationTime;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.expiration-time:3600000}") Long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * JWT 액세스 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userId)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * JWT 리프레시 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime * 7);

        return Jwts.builder()
                .subject(userId)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String extractUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 토큰 타입을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰 타입 ("access" 또는 "refresh")
     */
    public String extractTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * JWT 토큰이 액세스 토큰인지 검증합니다.
     *
     * @param token JWT 토큰
     * @return 액세스 토큰 여부
     */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    /**
     * JWT 리프레시 토큰을 검증합니다. 서명, 만료, 토큰 타입을 모두 확인합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 리프레시 토큰 여부
     */
    public boolean isValidRefreshToken(String token) {
        Claims claims = parseToken(token);
        return TOKEN_TYPE_REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * JWT 토큰을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public Boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception exception) {
            log.warn("JWT 토큰 검증 실패: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 반환합니다.
     *
     * @param token JWT 토큰
     * @return 클레임
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
