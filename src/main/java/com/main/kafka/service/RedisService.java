package com.main.kafka.service;

import com.main.kafka.dto.UserActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final Producer producer;


    public void sendUserActivity(UserActivity data) {
        producer.sendUserActivity(data);
    }
}
