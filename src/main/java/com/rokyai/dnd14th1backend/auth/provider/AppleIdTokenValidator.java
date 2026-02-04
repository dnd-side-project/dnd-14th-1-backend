package com.rokyai.dnd14th1backend.auth.provider;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.auth.exception.InvalidIdTokenException;

/**
 * Apple ID Token을 Apple의 공개 키로 서명 검증합니다. Apple의 JWKS 엔드포인트에서 공개 키를 조회하여 JWT 서명을 검증하고, 클레임을 추출합니다.
 */
@Component
public class AppleIdTokenValidator {

    private final AppleJwksProvider appleJwksProvider;
    private final String appleClientId;

    public AppleIdTokenValidator(
            AppleJwksProvider appleJwksProvider,
            @Value("${apple.client-id:}") String appleClientId) {
        this.appleJwksProvider = appleJwksProvider;
        this.appleClientId = appleClientId;
    }

    /**
     * Apple ID Token을 검증하고 디코딩된 JWT를 반환합니다.
     *
     * @param idToken Apple ID Token
     * @throws InvalidIdTokenException 검증 실패 시
     */
    public void validateAndDecode(String idToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(idToken);
            // Apple 공개키로 서명 검증 (원본 토큰 문자열 사용)
            verifySignature(idToken, decodedJWT);
            verifyClaims(decodedJWT);
        } catch (JWTVerificationException exception) {
            throw new InvalidIdTokenException("ID Token 서명 검증 실패", exception);
        } catch (Exception exception) {
            throw new InvalidIdTokenException("ID Token 검증 중 오류 발생", exception);
        }
    }

    /**
     * ID Token의 서명을 Apple의 공개 키로 검증합니다.
     *
     * @param idToken 원본 ID Token 문자열
     * @param decodedJWT 디코딩된 JWT (kid 추출용)
     * @throws InvalidIdTokenException 서명 검증 실패 시
     */
    private void verifySignature(String idToken, DecodedJWT decodedJWT) {
        try {
            String keyId = decodedJWT.getKeyId();
            if (keyId == null) {
                throw new InvalidIdTokenException(
                        AuthStatus.INVALID_ID_TOKEN, "ID Token 헤더에 kid(Key ID)가 없습니다");
            }

            PublicKey publicKey = appleJwksProvider.getPublicKey(keyId);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);

            // 원본 토큰 문자열로 검증 (DecodedJWT가 아닌)
            JWT.require(algorithm).withIssuer("https://appleid.apple.com").build().verify(idToken);
        } catch (InvalidIdTokenException exception) {
            throw exception;
        } catch (SignatureVerificationException exception) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN,
                    "Apple 공개 키로 서명 검증 실패: " + exception.getMessage(),
                    exception);
        } catch (Exception exception) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN,
                    "서명 검증 중 오류 발생: "
                            + exception.getClass().getSimpleName()
                            + " - "
                            + exception.getMessage(),
                    exception);
        }
    }

    private void verifyClaims(DecodedJWT decodedJWT) {
        // iss 클레임 검증
        String issuer = decodedJWT.getIssuer();
        if (!issuer.equals("https://appleid.apple.com")) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ISSUER,
                    String.format("잘못된 issuer: %s (expected: https://appleid.apple.com)", issuer));
        }

        // sub 클레임 검증: subject(사용자 ID)가 존재해야 함
        String subject = decodedJWT.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN, "ID Token에 sub(subject) 클레임이 없습니다");
        }

        // aud 클레임 검증: audience는 app의 Bundle ID 또는 등록된 client_id여야 함
        String audience =
                decodedJWT.getAudience().isEmpty() ? null : decodedJWT.getAudience().get(0);
        if (audience == null) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_ID_TOKEN, "ID Token에 aud(audience) 클레임이 없습니다");
        }

        // audience가 등록된 client_id와 일치하는지 검증
        if (!audience.equals(appleClientId)) {
            throw new InvalidIdTokenException(
                    AuthStatus.INVALID_AUDIENCE,
                    String.format("잘못된 audience: %s (expected: %s)", audience, appleClientId));
        }
    }
}
