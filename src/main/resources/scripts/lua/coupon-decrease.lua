-- KEYS[1]: 재고 키 (예: "coupon:stock:{couponId}")
-- KEYS[2]: 발급 이력 키 (예: "coupon:history:{couponId}")
-- ARGV[1]: 사용자 ID
-- ARGV[2]: 현재 시간 (타임스탬프)

-- 1. 재고 확인
local stock = tonumber(redis.call('GET', KEYS[1]))
if not stock or stock <= 0 then
    return 0  -- 재고 없음
end

-- 2. 중복 발급 검사
if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then
    return -1  -- 이미 발급됨
end

-- 3. 재고 감소 및 이력 저장 (원자적 연산)
redis.call('DECR', KEYS[1])
redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])
return 1  -- 발급 성공