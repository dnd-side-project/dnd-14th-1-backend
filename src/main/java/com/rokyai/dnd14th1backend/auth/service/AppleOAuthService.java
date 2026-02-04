package com.rokyai.dnd14th1backend.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.auth.dto.AppleIdTokenPayload;
import com.rokyai.dnd14th1backend.auth.dto.AppleOAuthRequest;
import com.rokyai.dnd14th1backend.auth.dto.AuthResponse;
import com.rokyai.dnd14th1backend.auth.enums.SigninType;
import com.rokyai.dnd14th1backend.auth.exception.InvalidIdTokenException;
import com.rokyai.dnd14th1backend.auth.provider.AppleIdTokenVerifier;
import com.rokyai.dnd14th1backend.auth.provider.JwtTokenProvider;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.domain.UserIdentity;
import com.rokyai.dnd14th1backend.users.infrastructure.UserIdentityRepository;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;

/** Apple OAuth 로그인을 처리합니다. */
@Service
@Transactional
public class AppleOAuthService {

    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    public AppleOAuthService(
            AppleIdTokenVerifier appleIdTokenVerifier,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository) {
        this.appleIdTokenVerifier = appleIdTokenVerifier;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
    }

    public AuthResponse authenticateWithApple(AppleOAuthRequest request) {
        String idToken = request.getIdToken();

        // ID Token 길이 검증 (Apple ID Token은 최소 100자 이상)
        if (idToken.length() < 100) {
            throw new InvalidIdTokenException(
                    "ID Token이 너무 짧습니다. 현재 길이: "
                            + idToken.length()
                            + "자 (정상적인 Apple ID Token은 1000자 이상입니다). "
                            + "올바른 identityToken을 전송했는지 확인하세요.");
        }

        // JWT 형식 검증 (점이 정확히 2개여야 함 = 3개 부분)
        int dotCount = (int) idToken.chars().filter(ch -> ch == '.').count();
        if (dotCount != 2) {
            throw new InvalidIdTokenException(
                    "ID Token 형식이 유효하지 않습니다. "
                            + "점(.)의 개수 - 예상: 2개, 실제: "
                            + dotCount
                            + "개. "
                            + "JWT는 'header.payload.signature' 형식이어야 합니다.");
        }

        // Apple ID Token을 검증하고 페이로드 추출
        AppleIdTokenPayload payload = appleIdTokenVerifier.verifyAndExtractPayload(idToken);

        // Apple 제공자 사용자 ID로 기존 아이덴티티 조회
        UserIdentity existingIdentity =
                userIdentityRepository
                        .findBySigninTypeAndProviderUserId(SigninType.APPLE, payload.getSubject())
                        .orElse(null);

        User user;
        boolean isNewUser;

        if (existingIdentity != null) {
            // 기존 사용자: 디바이스 정보 업데이트
            user = existingIdentity.getUser();
            existingIdentity.updateDeviceInfo(request.getDeviceId(), request.getPackageName());
            userIdentityRepository.save(existingIdentity);
            isNewUser = false;
        } else {
            // 신규 사용자: User와 UserIdentity 생성
            user = createNewUser(payload);
            createUserIdentity(user, payload, request);
            isNewUser = true;
        }

        String userId = user.getId().toString();

        // JWT 액세스 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userId);

        // JWT 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .isNewUser(isNewUser)
                .build();
    }

    /**
     * 새로운 사용자를 생성합니다.
     *
     * @param payload Apple ID Token 페이로드
     * @return 생성된 User
     */
    private User createNewUser(AppleIdTokenPayload payload) {
        User user = User.create(payload.getEmail());
        return userRepository.save(user);
    }

    /**
     * 사용자 아이덴티티를 생성합니다.
     *
     * @param user 사용자
     * @param payload Apple ID Token 페이로드
     * @param request Apple OAuth 요청
     */
    private void createUserIdentity(
            User user, AppleIdTokenPayload payload, AppleOAuthRequest request) {
        UserIdentity identity =
                UserIdentity.create(
                        user,
                        SigninType.APPLE,
                        payload.getSubject(),
                        request.getPlatform(),
                        request.getDeviceId(),
                        request.getPackageName());
        userIdentityRepository.save(identity);
    }
}
