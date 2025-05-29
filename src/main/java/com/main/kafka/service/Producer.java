package com.main.kafka.service;

import com.main.kafka.dto.SampleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Producer {

    private final KafkaTemplate<String, SampleData> kafkaTemplate;

    @Value("${app.default-topic}")
    private String topics;

    public void sendKafka(SampleData data) {
        log.info("topic : {}", topics);
        kafkaTemplate.send(topics, data);
    }
}
