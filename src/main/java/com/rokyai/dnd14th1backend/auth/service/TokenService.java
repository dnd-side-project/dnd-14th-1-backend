package com.rokyai.dnd14th1backend.auth.service;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;

import com.rokyai.dnd14th1backend.auth.dto.TokenRefreshResponse;
import com.rokyai.dnd14th1backend.auth.exception.AuthException;
import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.auth.exception.InvalidRefreshTokenException;
import com.rokyai.dnd14th1backend.auth.provider.JwtTokenProvider;

/** JWT 토큰 발급 및 갱신을 담당합니다. */
@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Refresh Token을 검증하고 새로운 토큰 쌍을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 리프레시 토큰
     * @throws InvalidRefreshTokenException 리프레시 토큰이 유효하지 않거나 타입이 올바르지 않은 경우
     * @throws AuthException 리프레시 토큰이 만료된 경우
     */
    public TokenRefreshResponse refreshToken(String refreshToken) {
        String userId;
        try {
            if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
                throw new InvalidRefreshTokenException();
            }
            userId = jwtTokenProvider.extractUserId(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthStatus.EXPIRED_TOKEN, "리프레시 토큰이 만료되었습니다");
        } catch (InvalidRefreshTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRefreshTokenException(e.getMessage());
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
