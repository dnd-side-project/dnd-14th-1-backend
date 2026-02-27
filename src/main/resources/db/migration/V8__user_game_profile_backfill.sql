-- 기존 유저 중 UserGameProfile이 없는 유저에 대해 프로필 생성
INSERT INTO user_game_profiles (id, user_id, total_xp, created_at, updated_at)
SELECT gen_random_uuid(), u.id, 0, NOW(), NOW()
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_game_profiles ugp WHERE ugp.user_id = u.id
);
