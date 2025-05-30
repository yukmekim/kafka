package com.main.kafka.controller;

import com.main.kafka.dto.UserActivity;
import com.main.kafka.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
public class KafkaController {
    private final RedisService redisService;

    @PostMapping("/send")
    public String sendData(@RequestBody UserActivity data) {
        redisService.sendUserActivity(data);
        return "yes";
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserActivity>> getUserActivities(@PathVariable String userId) {
        return ResponseEntity.ok(redisService.getUserActivities(userId));
    }
}
