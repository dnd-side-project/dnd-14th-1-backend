package com.rokyai.dnd14th1backend.users.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.common.util.UuidV7;

/** 사용자. */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_user_badge_id")
    private UserBadge representativeUserBadge;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserIdentity> identities = new ArrayList<>();

    /**
     * 사용자를 생성합니다.
     *
     * @param email 이메일 주소
     * @param name 사용자 이름
     * @return 생성된 User
     */
    public static User create(String email, String name) {
        return User.builder().email(email).name(name).build();
    }

    /**
     * 대표 배지를 설정합니다.
     *
     * @param userBadge 대표로 설정할 유저-배지 연관 엔티티
     */
    public void updateRepresentativeUserBadge(UserBadge userBadge) {
        this.representativeUserBadge = userBadge;
    }
}
