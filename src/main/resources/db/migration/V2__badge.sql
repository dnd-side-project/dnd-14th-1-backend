CREATE TABLE badge (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    trigger_condition INTEGER NOT NULL,
    enable_image_url VARCHAR(500) NOT NULL,
    disable_image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_badge (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    badge_id UUID NOT NULL,
    earned_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES badge (id),
    CONSTRAINT uk_user_badge UNIQUE (user_id, badge_id)
);

CREATE INDEX idx_user_badge_user_id ON user_badge (user_id);

-- 배지 시드 데이터 (trigger_condition: 달성 필요 횟수)
INSERT INTO badge (id, name, description, tier, trigger_condition, enable_image_url, disable_image_url, created_at, updated_at) VALUES
    ('01968e00-0000-7000-8000-000000000001', '빙하 가디언', '리소스 절감 여정에 첫발을 내디딘 유저를 환영', 'BEGINNER', 1, '/badge/enable/enable_gadian.png', '/badge/disable/disable_gadian.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000002', '원샷 패스', '한 번의 시도로 완벽한 결과를 이끌어낸 효율의 달인', 'BEGINNER', 1, '/badge/enable/enable_oneshot.png', '/badge/disable/disable_oneshot.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000003', '에코 그리팅', '환경을 생각하는 따뜻한 인사를 건넨 유저', 'BEGINNER', 3, '/badge/enable/enable_ecogriting.png', '/badge/disable/disable_ecogriting.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000004', '슬림 프롬프트', '간결하고 효율적인 프롬프트의 마스터', 'BEGINNER', 5, '/badge/enable/enable_slimpt.png', '/badge/disable/disable_slimpt.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000005', '빙하의 발자국', '꾸준히 환경을 생각하며 걸어온 발자국', 'BEGINNER', 7, '/badge/enable/enable_foot.png', '/badge/disable/disable_foot.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000006', '탄소 킬러', '탄소 배출을 획기적으로 줄인 환경 전사', 'INTERMEDIATE', 1, '/badge/enable/enable_killer.png', '/badge/disable/disable_killer.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000007', '제로 웨이스트', '불필요한 리소스 낭비를 완전히 제거한 유저', 'INTERMEDIATE', 10, '/badge/enable/enable_zeroweist.png', '/badge/disable/disable_zeroweist.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000008', '빙하 복구사', '빙하 보호에 실질적으로 기여한 복원 전문가', 'INTERMEDIATE', 5, '/badge/enable/enable_icebergheal.png', '/badge/disable/disable_icebergheal.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-000000000009', '북극의 심장', '북극 생태계를 지키는 핵심 수호자', 'ADVANCED', 8, '/badge/enable/enable_heart.png', '/badge/disable/disable_heart.png', NOW(), NOW()),
    ('01968e00-0000-7000-8000-00000000000a', '절대영도 마스터', '최고 수준의 환경 보호 달성자', 'ADVANCED', 10, '/badge/enable/enable_master.png', '/badge/disable/disable_master.png', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
