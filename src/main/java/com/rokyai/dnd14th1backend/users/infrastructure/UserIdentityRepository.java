package com.rokyai.dnd14th1backend.users.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.auth.enums.SigninType;
import com.rokyai.dnd14th1backend.users.domain.UserIdentity;

/** UserIdentity 저장소. */
@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    /**
     * OAuth 제공자별 사용자 ID로 아이덴티티를 조회합니다.
     *
     * @param signinType OAuth 제공자
     * @param providerUserId 제공자의 사용자 ID
     * @return 아이덴티티
     */
    Optional<UserIdentity> findBySigninTypeAndProviderUserId(
            SigninType signinType, String providerUserId);
}
