package com.rokyai.dnd14th1backend.users.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rokyai.dnd14th1backend.users.domain.User;

/** User 저장소. */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 이메일
     * @return 사용자
     */
    Optional<User> findByEmail(String email);
}
