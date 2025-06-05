package com.main.kafka.service;

import com.main.kafka.dto.CouponEvent;
import com.main.kafka.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
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

        // 트랜잭션 처리보다는 lua 스크립트쪽이 원자성이 보장되는거 같음
        Long result = decreaseCouponStockScript(couponId, userId);

        if (result == null || result == 0) {
            return Response.payload(false, "쿠폰 소진");
        } else {
            CouponEvent event = CouponEvent.builder()
                    .couponId(couponId)
                    .userId(userId)
                    .eventType(CouponEvent.EventType.ISSUE)
                    .time(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(topics, event);
            return Response.payload(true, event,"쿠폰 발급 이력 저장");
        }
    }

    public void decreaseStock(String couponId, String userId) {
        log.info("요청자 : {}", userId);
        if (hasAlreadyAcquired(couponId, userId)) {
            return;
        }
        // lua 스크립트 테스트
        Long result = decreaseCouponStockScript(couponId, userId);

        if (result == null || result == 0) {
            log.info("쿠폰 재고 소진 : {}", couponId);
        } else if (result == -1){
            log.info("이미 발급 받은 사용자 : {}", userId);
        } else {
            CouponEvent event = CouponEvent.builder()
                    .couponId(couponId)
                    .userId(userId)
                    .eventType(CouponEvent.EventType.ISSUE)
                    .time(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(topics, event);
            log.info("쿠폰 획득 : {}", userId);
        }


        // 트랜잭션 테스트
//        boolean locked = redisTemplate.opsForValue()
//                .setIfAbsent(LOCK_KEY_PREFIX + couponId,
//                        userId,
//                        1, TimeUnit.SECONDS);
//
//        if (!locked) {
//            log.info("시스템이 혼잡한 상태 : {}, userId : {}", couponId, userId);
//            return;
//        }
//
//        boolean acquired = decreaseCouponStock(couponId, userId);
//        if (acquired) {
//          log.info("쿠폰 획득 : {}", userId);
//        }
//        log.info("쿠폰 재고 소진 : {}", couponId);
    }

    public Long decreaseCouponStockScript(String couponId, String userId) {
        // 2. Lua 스크립트 정의 (직접 문자열로 작성)
        String luaScript =
                "local stockKey = KEYS[1]\n" +
                        "local historyKey = KEYS[2]\n" +
                        "local userId = ARGV[1]\n" +
                        "local timestamp = ARGV[2]\n" +
                        "if tonumber(redis.call('GET', stockKey)) <= 0 then\n" +
                        "    return 0\n" +
                        "end\n" +
                        "if redis.call('HEXISTS', historyKey, userId) == 1 then\n" +
                        "    return -1\n" +
                        "end\n" +
                        "redis.call('DECR', stockKey)\n" +
                        "redis.call('HSET', historyKey, userId, timestamp)\n" +
                        "return 1";

        // 3. 스크립트 실행
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        List<String> keys = Arrays.asList(
                STOCK_KEY_PREFIX + couponId,
                HISTORY_KEY_PREFIX + couponId
        );

        return redisTemplate.execute(
                redisScript,
                keys,
                userId,
                LocalDateTime.now().toString()
        );
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

    public int remainCouponStock(String couponId) {
        return (Integer) redisTemplate.opsForValue()
                .get(STOCK_KEY_PREFIX + couponId);
    }

    public Map<Object, Object> couponAcquiredList(String couponId) {
        return redisTemplate.opsForHash()
                .entries(HISTORY_KEY_PREFIX + couponId);
    }
}
