package com.example.demo.controller;

import com.example.demo.service.KhqrService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/khqr")
@RequiredArgsConstructor
@Slf4j
public class KhqrController {

    private final KhqrService khqrService;

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        try {
            String qr = khqrService.generateQr();
            String md5 = khqrService.generateMd5(qr);
            boolean isPaid = khqrService.checkPayment(qr);

            response.put("qrData", qr);
            response.put("md5", md5);
            response.put("isPaid", isPaid);
            response.put("success", true);

            log.info("Generated QR successfully with MD5: {}", md5);

        } catch (Exception e) {
            log.error("Error in test endpoint", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}