package com.main.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CouponEvent implements Serializable {
    public enum EventType {
        ISSUE, CANCEL
    }

    private String eventId;
    private String userId;
    private String couponCode;
    private EventType eventType;
    private LocalDateTime time;

    public CouponEvent() {
        this.time = LocalDateTime.now();
    }
}
