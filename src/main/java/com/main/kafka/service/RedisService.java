package com.main.kafka.service;

import com.main.kafka.dto.SampleData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DATA_KEY = "data:1";

    public void saveData(SampleData data) {
        redisTemplate.opsForList().leftPush(DATA_KEY, data); // 리스트 왼쪽부터 추가
        redisTemplate.opsForList().trim(DATA_KEY, 0, 99);
    }

    public List<SampleData> getRecentData(int no) {
        return redisTemplate.opsForList().range(DATA_KEY, 0, no -1)
                .stream()
                .map(data -> (SampleData) data)
                .collect(Collectors.toList());
    }
}
