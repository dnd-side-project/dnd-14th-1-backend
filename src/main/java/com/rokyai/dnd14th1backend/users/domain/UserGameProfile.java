package com.rokyai.dnd14th1backend.users.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.common.util.UuidV7;

/** 사용자 게임 프로필 엔티티 (XP/티어 관리) */
@Entity
@Table(name = "user_game_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserGameProfile {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private long totalXp = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static UserGameProfile create(UUID userId) {
        return UserGameProfile.builder().userId(userId).build();
    }

    public void addXp(int xp) {
        this.totalXp += xp;
    }
}
