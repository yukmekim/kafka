package com.main.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_PREFIX = "coupon:stock:";

    /**
     * 쿠폰 재고 초기화
     * @param couponId 쿠폰 ID
     * @param quantity 재고량
     * */
    public void initializeCouponStock(String couponId, int quantity) {
        // 1시간 뒤에 쿠폰 만료
        redisTemplate.opsForValue().set(
                STOCK_PREFIX + couponId,
                quantity,
                1, TimeUnit.HOURS
        );
    }
}
