package com.rokyai.dnd14th1backend.auth.service;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.auth.dto.AuthResponse;
import com.rokyai.dnd14th1backend.auth.dto.CreateTestUserRequest;
import com.rokyai.dnd14th1backend.auth.enums.Platform;
import com.rokyai.dnd14th1backend.auth.enums.SigninType;
import com.rokyai.dnd14th1backend.auth.provider.JwtTokenProvider;
import com.rokyai.dnd14th1backend.badge.service.DefaultBadgeService;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.domain.UserIdentity;
import com.rokyai.dnd14th1backend.users.infrastructure.UserIdentityRepository;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;

/** 테스트 사용자 생성 서비스. dev 환경에서만 활성화됩니다. */
@Service
@Profile("dev")
@Transactional
public class TestAuthService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final DefaultBadgeService defaultBadgeService;

    public TestAuthService(
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            JwtTokenProvider jwtTokenProvider,
            DefaultBadgeService defaultBadgeService) {
        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.defaultBadgeService = defaultBadgeService;
    }

    /**
     * 테스트 사용자를 생성하고 인증 토큰을 반환합니다.
     *
     * @param request 테스트 사용자 생성 요청
     * @return 액세스 토큰과 사용자 정보
     */
    public AuthResponse createTestUser(CreateTestUserRequest request) {
        String email =
                request.getEmail() != null
                        ? request.getEmail()
                        : "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";

        User user = User.create(email, null);
        user = userRepository.save(user);
        defaultBadgeService.ensureDefaultBadge(user);

        UserIdentity identity =
                UserIdentity.create(
                        user,
                        SigninType.APPLE,
                        "test-provider-" + user.getId(),
                        Platform.IOS,
                        "test-device",
                        "com.rokyai.test");
        userIdentityRepository.save(identity);

        String userId = user.getId().toString();
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .isNewUser(true)
                .build();
    }
}
