package com.rokyai.dnd14th1backend.auth.provider;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** JWT 토큰 생성 및 검증을 담당합니다. */
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

    /**
     * JWT 액세스 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT 토큰
     */
    public String generateAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userId)
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
        Date expiryDate = new Date(now.getTime() + expirationTime * 7); // 액세스 토큰의 7배 유효기간

        return Jwts.builder()
                .subject(userId)
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
