package com.main.kafka.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("Redis 동시 발급 테스트")
    void couponAcquiredTest() throws InterruptedException {
        String couponId = "1";

        int count = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            String userId = String.valueOf(i);
            executorService.submit(() -> {
                couponService.decreaseStock(couponId, userId);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Integer stock = couponService.remainCouponStock(couponId);
        System.out.printf("남은 수량 %s", stock);
        assertEquals(0, stock);
    }
}