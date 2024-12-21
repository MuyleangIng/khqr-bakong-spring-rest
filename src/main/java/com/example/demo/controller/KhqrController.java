package com.example.demo.controller;

import com.example.demo.model.PaymentRequest;
import com.example.demo.model.PaymentStatusResponse;
import com.example.demo.model.QrResponse;
import com.example.demo.service.KhqrService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/khqr")
@RequiredArgsConstructor
@Slf4j
public class KhqrController {

    private final KhqrService khqrService;

    @PostMapping("/generate")
    public QrResponse generateQr(@RequestBody PaymentRequest request) {
        return khqrService.generateQr(request);
    }

    @GetMapping("/verify/{md5Hash}")
    public PaymentStatusResponse verifyPayment(@PathVariable String md5Hash) {
        return khqrService.checkPayment(md5Hash);
    }
}