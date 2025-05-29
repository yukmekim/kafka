package com.main.kafka.controller;

import com.main.kafka.dto.SampleData;
import com.main.kafka.service.Producer;
import com.main.kafka.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kafka")
public class KafkaController {
    private final Producer producer;
    private final RedisService redisService;

    @PostMapping("/send")
    public String sendData(@RequestBody SampleData data) {
        producer.sendKafka(data);
        return "yes";
    }

    @GetMapping("/get")
    public ResponseEntity<List<SampleData>> getRecentData(@RequestParam(defaultValue = "10") int no) {
        return ResponseEntity.ok(redisService.getRecentData(no));
    }
}
