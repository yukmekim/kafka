package com.main.kafka.service;

import com.main.kafka.dto.UserActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Producer {

    private final KafkaTemplate<String, UserActivity> kafkaTemplate;

    @Value("${app.default-topic}")
    private String topics;

    public void sendUserActivity(UserActivity data) {
        log.info("topic : {}", topics);
        kafkaTemplate.send(topics, data);
    }
}
