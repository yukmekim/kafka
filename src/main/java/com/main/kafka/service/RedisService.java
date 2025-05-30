package com.main.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.kafka.dto.UserActivity;
import com.main.kafka.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final Producer producer;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    public void sendUserActivity(UserActivity data) {
        producer.sendUserActivity(data);
    }

    public List<UserActivity> getUserActivities(String userId) {
        Set<Object> activities = redisRepository.getUserActivities(userId);
        return activities.stream()
                .map(data -> objectMapper.convertValue(data, UserActivity.class))
                .toList();
    }
}
