package com.rokyai.dnd14th1backend.users.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.users.domain.UserGameProfile;

/** 사용자 게임 프로필 리포지토리 */
@Repository
public interface UserGameProfileRepository extends JpaRepository<UserGameProfile, UUID> {

    Optional<UserGameProfile> findByUserId(UUID userId);
}
