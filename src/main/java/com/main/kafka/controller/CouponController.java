package com.main.kafka.controller;

import com.main.kafka.dto.Response;
import com.main.kafka.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/init/{couponId}")
    public ResponseEntity<Response<Void>> initializeCouponStock(@PathVariable String couponId,
                                                             @RequestParam(defaultValue = "100") int quantity) {
        couponService.initializeCouponStock(couponId, quantity);
        return ResponseEntity.ok(Response.result(true, "초기화"));
    }
}
