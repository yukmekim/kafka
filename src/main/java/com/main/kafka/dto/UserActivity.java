package com.main.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class UserActivity implements Serializable {
    private String userId;
    private String activityType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime time;
    private String item;
    private Map<String, String> data;

    public UserActivity() {
        this.time = LocalDateTime.now();
    }
}
