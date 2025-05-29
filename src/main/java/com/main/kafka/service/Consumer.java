package com.main.kafka.service;

import com.main.kafka.dto.SampleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {

    private final RedisService redisService;

    @KafkaListener(topics = "${app.default-topic}")
    public void consume(SampleData data) {
        redisService.saveData(data);
        log.info("Received data : {}", data);
    }
}
