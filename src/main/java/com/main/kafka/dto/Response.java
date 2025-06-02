package com.main.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> Response<T> payload(boolean success, String message) {
        return Response.<T>builder()
                .message(message)
                .success(success)
                .build();
    }

    public static <T> Response<T> payload(boolean success, T data, String message) {
        return Response.<T>builder()
                .success(success)
                .data(data)
                .message(message)
                .build();
    }
}
