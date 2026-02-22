-- users 테이블에 대표 배지 FK 컬럼 추가 (user_badge 참조)
ALTER TABLE users ADD COLUMN representative_user_badge_id UUID;

ALTER TABLE users ADD CONSTRAINT fk_users_representative_user_badge
    FOREIGN KEY (representative_user_badge_id) REFERENCES user_badge (id);
