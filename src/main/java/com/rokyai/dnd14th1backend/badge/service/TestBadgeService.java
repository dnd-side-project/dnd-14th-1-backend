package com.rokyai.dnd14th1backend.badge.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;
import com.rokyai.dnd14th1backend.badge.dto.GrantTestBadgeRequest;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.mapper.BadgeMapper;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;
import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.domain.Conversation;
import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.CrawlingTaskRepository;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.domain.UserGameProfile;
import com.rokyai.dnd14th1backend.users.infrastructure.UserGameProfileRepository;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;

/** 테스트 배지 부여 서비스. dev 환경에서만 활성화됩니다. */
@Service
@Profile("dev")
@Transactional
public class TestBadgeService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserGameProfileRepository userGameProfileRepository;
    private final ChatRepository chatRepository;
    private final ConversationRepository conversationRepository;
    private final CrawlingTaskRepository crawlingTaskRepository;

    public TestBadgeService(
            UserRepository userRepository,
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            UserGameProfileRepository userGameProfileRepository,
            ChatRepository chatRepository,
            ConversationRepository conversationRepository,
            CrawlingTaskRepository crawlingTaskRepository) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userGameProfileRepository = userGameProfileRepository;
        this.chatRepository = chatRepository;
        this.conversationRepository = conversationRepository;
        this.crawlingTaskRepository = crawlingTaskRepository;
    }

    /**
     * 테스트용으로 특정 유저에게 배지를 부여하고, 해당 배지의 획득 조건에 맞게 진행 데이터를 조정합니다.
     *
     * @param request 배지 부여 요청 (userId, badgeId)
     * @return 부여된 배지 정보
     * @throws IllegalArgumentException 사용자/배지를 찾을 수 없거나 이미 보유한 경우
     */
    public UserBadgeResponse grantBadge(GrantTestBadgeRequest request) {
        UUID userId = request.getUserId();
        UUID badgeId = request.getBadgeId();

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Badge badge =
                badgeRepository
                        .findById(badgeId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("배지를 찾을 수 없습니다: " + badgeId));

        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            throw new IllegalArgumentException("이미 보유한 배지입니다: " + badgeId);
        }

        adjustProgressData(userId, badge);

        UserBadge userBadge =
                UserBadge.builder().user(user).badge(badge).earnedAt(LocalDateTime.now()).build();
        userBadgeRepository.save(userBadge);

        if (user.getRepresentativeUserBadge() == null) {
            user.updateRepresentativeUserBadge(userBadge);
        }

        UUID representativeUserBadgeId =
                user.getRepresentativeUserBadge() != null
                        ? user.getRepresentativeUserBadge().getId()
                        : null;

        return BadgeMapper.toUserBadgeResponse(userBadge, representativeUserBadgeId);
    }

    /** 배지의 triggerType에 따라 유저의 진행 데이터를 조건 이상으로 조정합니다. */
    private void adjustProgressData(UUID userId, Badge badge) {
        int triggerCondition = badge.getTriggerCondition();
        BadgeTriggerType triggerType = badge.getTriggerType();

        switch (triggerType) {
            case CUMULATIVE_XP -> adjustCumulativeXp(userId, triggerCondition);
            case OPTIMIZE_COUNT -> adjustOptimizeCount(userId, triggerCondition);
            case SINGLE_TOKEN_SAVING -> adjustSingleTokenSaving(userId, triggerCondition);
            case CRAWLING_COUNT -> adjustCrawlingCount(userId, triggerCondition);
        }
    }

    private void adjustCumulativeXp(UUID userId, int triggerCondition) {
        UserGameProfile profile =
                userGameProfileRepository
                        .findByUserId(userId)
                        .orElseGet(
                                () ->
                                        userGameProfileRepository.save(
                                                UserGameProfile.create(userId)));

        long diff = triggerCondition - profile.getTotalXp();
        if (diff > 0) {
            profile.addXp((int) diff);
        }
    }

    private void adjustOptimizeCount(UUID userId, int triggerCondition) {
        long currentCount = chatRepository.countOptimizedByUserId(userId);
        long deficit = triggerCondition - currentCount;
        if (deficit <= 0) {
            return;
        }

        Conversation conversation = getOrCreateTestConversation(userId);
        int startSequence = chatRepository.findMaxSequenceByConversationId(conversation.getId());

        for (long i = 0; i < deficit; i++) {
            Chat chat =
                    Chat.create(conversation, "[테스트] 최적화 mock 데이터", "[테스트] 응답", ++startSequence);
            chat.applyOptimization(1, 1);
            chatRepository.save(chat);
        }
    }

    private void adjustSingleTokenSaving(UUID userId, int triggerCondition) {
        int currentMax = chatRepository.findMaxTokenSavingByUserId(userId);
        if (currentMax >= triggerCondition) {
            return;
        }

        Conversation conversation = getOrCreateTestConversation(userId);
        int nextSequence = chatRepository.findMaxSequenceByConversationId(conversation.getId()) + 1;

        Chat chat = Chat.create(conversation, "[테스트] 토큰절약 mock 데이터", "[테스트] 응답", nextSequence);
        chat.applyOptimization(triggerCondition, 1);
        chatRepository.save(chat);
    }

    private void adjustCrawlingCount(UUID userId, int triggerCondition) {
        long currentCount = crawlingTaskRepository.countByUserId(userId);
        long deficit = triggerCondition - currentCount;
        if (deficit <= 0) {
            return;
        }

        for (long i = 0; i < deficit; i++) {
            CrawlingTask task =
                    CrawlingTask.create("https://test.mock/" + i, Platform.OTHER, userId);
            task.markCompleted();
            crawlingTaskRepository.save(task);
        }
    }

    private Conversation getOrCreateTestConversation(UUID userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .findFirst()
                .orElseGet(
                        () -> {
                            Conversation conversation =
                                    Conversation.createByUser(userId, "[테스트] mock 대화");
                            return conversationRepository.save(conversation);
                        });
    }
}
