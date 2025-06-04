package com.main.kafka.service;

import com.main.kafka.dto.CouponEvent;
import com.main.kafka.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, CouponEvent> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topics;

    private static final String STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String HISTORY_KEY_PREFIX = "coupon:history:";
    private static final String LOCK_KEY_PREFIX = "coupon:lock:";

    /**
     * 쿠폰 재고 초기화
     * @param couponId 쿠폰 ID
     * @param quantity 재고량
     * */
    public void initializeCouponStock(String couponId, int quantity) {
        // 1시간 뒤에 쿠폰 만료
        redisTemplate.opsForValue().set(
                STOCK_KEY_PREFIX + couponId,
                quantity,
                30, TimeUnit.MINUTES
        );
    }

    /**
     * 쿠폰 획득 이벤트
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * */
    @Transactional
    public Response<CouponEvent> acquiredCoupon(String couponId, String userId) {
        if (hasAlreadyAcquired(couponId, userId)) {
            return Response.payload(false, "이미 발급된 쿠폰");
        }

        boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY_PREFIX + couponId,
                userId,
                5, TimeUnit.SECONDS);

        if(!locked) {
            return Response.payload(false, "시스템이 혼잡한 상태");
        }

        boolean acquired = decreaseCouponStock(couponId, userId);
        if (acquired) {
            CouponEvent event = CouponEvent.builder()
                    .couponId(couponId)
                    .userId(userId)
                    .eventType(CouponEvent.EventType.ISSUE)
                    .time(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(topics, event);
            return Response.payload(true, event,"쿠폰 발급 이력 저장");
        }
        return Response.payload(false, "쿠폰 소진");
    }

    /**
     * 쿠폰 발급 이력 확인
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * @return boolean 이력 유무 확인
     * */
    private boolean hasAlreadyAcquired(String couponId, String userId) {
        return redisTemplate.opsForHash()
                .hasKey(HISTORY_KEY_PREFIX + couponId, userId);
    }

    private boolean decreaseCouponStock(String couponId, String userId) {
        return redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                operations.watch(STOCK_KEY_PREFIX + couponId);

                Integer stock = (Integer) operations.opsForValue().get(STOCK_KEY_PREFIX + couponId);
                if (stock == null || stock <= 0) {
                    operations.unwatch();
                    return false;
                }

                operations.multi();
                operations.opsForValue().decrement(STOCK_KEY_PREFIX + couponId);
                operations.opsForHash().put(HISTORY_KEY_PREFIX + couponId,
                        userId,
                        LocalDateTime.now());
                List<Object> result = operations.exec();
                return !result.isEmpty();
            }
        });
    }
}
