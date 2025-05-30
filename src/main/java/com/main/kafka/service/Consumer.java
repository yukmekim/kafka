package com.main.kafka.service;

import com.main.kafka.dto.UserActivity;
import com.main.kafka.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {

    //private final RedisService redisService;
    private final RedisRepository redisRepository;

    @KafkaListener(topics = "${app.default-topic}")
    public void consume(UserActivity data) {
        redisRepository.addUserActivity(data);
        log.info("Received data : {}", data);
    }
}
