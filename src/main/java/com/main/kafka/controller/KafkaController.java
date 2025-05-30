package com.main.kafka.controller;

import com.main.kafka.dto.UserActivity;
import com.main.kafka.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kafka")
public class KafkaController {
    private final RedisService redisService;

    @PostMapping("/send")
    public String sendData(@RequestBody UserActivity data) {
        redisService.sendUserActivity(data);
        return "yes";
    }
}
