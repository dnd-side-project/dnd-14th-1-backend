package com.rokyai.dnd14th1backend.badge.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rokyai.dnd14th1backend.badge.entity.Badge;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {}
