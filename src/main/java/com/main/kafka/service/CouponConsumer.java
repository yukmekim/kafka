package com.main.kafka.service;

import com.main.kafka.dto.CouponEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String HISTORY_KEY_PREFIX = "coupon:history:";

    @KafkaListener(topics = "${spring.kafka.template.default-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleCouponEvent(CouponEvent event) {
        boolean history = redisTemplate.opsForHash().hasKey(HISTORY_KEY_PREFIX + event.getCouponId(),
                event.getUserId());

        if (history) {
            log.info("쿠폰 데이터 저장 : {}, {}", event.getCouponId(), event.getUserId());
        }
    }
}
