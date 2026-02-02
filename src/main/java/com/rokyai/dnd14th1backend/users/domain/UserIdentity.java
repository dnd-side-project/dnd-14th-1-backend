package com.rokyai.dnd14th1backend.users.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.auth.enums.Platform;
import com.rokyai.dnd14th1backend.auth.enums.SigninType;
import com.rokyai.dnd14th1backend.common.util.UuidV7;

/** 사용자 아이덴티티 (OAuth 제공자별 계정 연결 정보). 한 명의 사용자는 여러 개의 OAuth 제공자 계정을 가질 수 있습니다. */
@Entity
@Table(name = "user_identities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserIdentity {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SigninType signinType;

    @Column(nullable = false)
    private String providerUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 사용자 아이덴티티를 생성합니다.
     *
     * @param user 사용자
     * @param signinType OAuth 제공자
     * @param providerUserId 제공자의 사용자 ID
     * @param platform 플랫폼
     * @param deviceId 디바이스 ID
     * @param packageName 앱 패키지명
     * @return 생성된 UserIdentity
     */
    public static UserIdentity create(
            User user,
            SigninType signinType,
            String providerUserId,
            Platform platform,
            String deviceId,
            String packageName) {
        LocalDateTime now = LocalDateTime.now();
        return UserIdentity.builder()
                .user(user)
                .signinType(signinType)
                .providerUserId(providerUserId)
                .platform(platform)
                .deviceId(deviceId)
                .packageName(packageName)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 아이덴티티 정보를 업데이트합니다.
     *
     * @param deviceId 새 디바이스 ID
     * @param packageName 새 패키지명
     */
    public void updateDeviceInfo(String deviceId, String packageName) {
        this.deviceId = deviceId;
        this.packageName = packageName;
        this.updatedAt = LocalDateTime.now();
    }
}
