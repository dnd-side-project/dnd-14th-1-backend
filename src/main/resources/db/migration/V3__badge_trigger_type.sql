-- trigger_type 컬럼 추가 (임시 DEFAULT로 NOT NULL 제약 충족)
ALTER TABLE badge ADD COLUMN trigger_type VARCHAR(50) NOT NULL DEFAULT 'OPTIMIZE_COUNT';
ALTER TABLE badge ALTER COLUMN trigger_type DROP DEFAULT;

-- 배지 이름, trigger_type, trigger_condition 업데이트
UPDATE badge
SET name = '빙하 지킴이', trigger_type = 'OPTIMIZE_COUNT', trigger_condition = 1
WHERE id = '01968e00-0000-7000-8000-000000000001';

UPDATE badge
SET name = '3점 슛!', trigger_type = 'SINGLE_TOKEN_SAVING', trigger_condition = 500
WHERE id = '01968e00-0000-7000-8000-000000000002';

UPDATE badge
SET name = '무럭무럭', trigger_type = 'CRAWLING_COUNT', trigger_condition = 1
WHERE id = '01968e00-0000-7000-8000-000000000003';

UPDATE badge
SET name = '슬림 프롬프트', trigger_type = 'OPTIMIZE_COUNT', trigger_condition = 5
WHERE id = '01968e00-0000-7000-8000-000000000004';

UPDATE badge
SET name = '한 발자국 더', trigger_type = 'OPTIMIZE_COUNT', trigger_condition = 10
WHERE id = '01968e00-0000-7000-8000-000000000005';

UPDATE badge
SET name = '탄소 킬러', trigger_type = 'SINGLE_TOKEN_SAVING', trigger_condition = 2000
WHERE id = '01968e00-0000-7000-8000-000000000006';

UPDATE badge
SET name = '제로 웨이스트', trigger_type = 'OPTIMIZE_COUNT', trigger_condition = 30
WHERE id = '01968e00-0000-7000-8000-000000000007';

UPDATE badge
SET name = '빙하 복구 전문가', trigger_type = 'CUMULATIVE_XP', trigger_condition = 5000
WHERE id = '01968e00-0000-7000-8000-000000000008';

UPDATE badge
SET name = '북극의 심장', trigger_type = 'OPTIMIZE_COUNT', trigger_condition = 100
WHERE id = '01968e00-0000-7000-8000-000000000009';

UPDATE badge
SET name = '세이빙 마스터', trigger_type = 'CUMULATIVE_XP', trigger_condition = 50000
WHERE id = '01968e00-0000-7000-8000-00000000000a';
