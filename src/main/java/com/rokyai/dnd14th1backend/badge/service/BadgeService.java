package com.rokyai.dnd14th1backend.badge.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.mapper.BadgeMapper;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;

/** 배지 비즈니스 로직. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;

    /** 전체 배지 목록을 조회합니다. */
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream().map(BadgeMapper::toResponse).toList();
    }
}
