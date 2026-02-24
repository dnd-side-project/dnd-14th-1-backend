package com.rokyai.dnd14th1backend.users.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.dto.UserMeResponse;
import com.rokyai.dnd14th1backend.users.exception.UserGameErrorStatus;
import com.rokyai.dnd14th1backend.users.exception.UserGameException;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;
import com.rokyai.dnd14th1backend.users.mapper.UserMapper;

/** 사용자 서비스. */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 현재 로그인한 사용자의 정보를 조회합니다.
     *
     * @param userId 인증된 사용자 ID
     * @return 사용자 정보 (대표 배지 포함)
     */
    public UserMeResponse getMe(UUID userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new UserGameException(UserGameErrorStatus.USER_NOT_FOUND));
        return UserMapper.toMeResponse(user);
    }
}
