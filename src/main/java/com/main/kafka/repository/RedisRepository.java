package com.main.kafka.repository;

import com.main.kafka.dto.UserActivity;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.Set;

@Repository
@AllArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DATA_KEY = "user:activity:";
    private static final String RECENT_KEY = "recent:activity";
    private static final int DATA_LIMIT = 100;

    public void addUserActivity(UserActivity userActivity) {
        String key = DATA_KEY + userActivity.getSender();

        // 등록된 시간순으로 정렬해서 등록
        redisTemplate.opsForZSet().add(
                key,
                userActivity,
                userActivity.getTime().atZone(ZoneId.systemDefault()).toEpochSecond());

        redisTemplate.opsForZSet().removeRange(key, 0, -DATA_LIMIT -1);

        redisTemplate.opsForList().leftPush(RECENT_KEY, userActivity);
        redisTemplate.opsForList().trim(RECENT_KEY, 0, DATA_LIMIT -1);
    }

    public Set<Object> getUserActivities(String userId) {
        String key = DATA_KEY + userId;
        return redisTemplate.opsForZSet().reverseRange(key, 0, DATA_LIMIT);
    }
}
