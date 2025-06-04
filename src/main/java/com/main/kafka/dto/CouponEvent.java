package com.main.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CouponEvent implements Serializable {
    public enum EventType {
        ISSUE, CANCEL
    }

    private String couponId;
    private String userId;
    private String couponCode;
    private EventType eventType;
    private LocalDateTime time;
}
