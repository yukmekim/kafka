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
    private String message;
    private T data;

    public static <T> Response<T> result(boolean success, String message) {
        return Response.<T>builder()
                .message(message)
                .success(success)
                .build();
    }

    public static <T> Response<T> result(boolean success, String message, T data) {
        return Response.<T>builder()
                .message(message)
                .success(success)
                .data(data)
                .build();
    }
}
