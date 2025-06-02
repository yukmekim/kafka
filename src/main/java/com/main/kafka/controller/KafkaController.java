package com.main.kafka.controller;

import com.main.kafka.dto.Response;
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
    public ResponseEntity<Response<List<UserActivity>>> getUserActivities(@PathVariable String userId) {
        List<UserActivity> data = redisService.getUserActivities(userId);
        return ResponseEntity.ok(Response.result(true, "dd", data));
    }
}
