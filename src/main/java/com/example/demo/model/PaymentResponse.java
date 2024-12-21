package com.example.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String qrCode;
    private String qrImageBase64;
    private String deeplink;
    private String transactionId;
}
