-- 기본 지급 배지(000001)를 정책과 정합화
UPDATE badge
SET name = '빙하 지킴이',
    description = '처음 가입 시 자동으로 지급되는 기본 뱃지',
    trigger_condition = 0,
    updated_at = NOW()
WHERE id = '01968e00-0000-7000-8000-000000000001'::UUID;

-- 이름 정책 유지 (000005)
UPDATE badge
SET name = '한 발자국 더',
    updated_at = NOW()
WHERE id = '01968e00-0000-7000-8000-000000000005'::UUID;
