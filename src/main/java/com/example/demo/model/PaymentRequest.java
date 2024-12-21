package com.example.demo.model;

public record PaymentRequest(
        Double amount,
        String currency,
        String merchantName,
        String bankAccount,
        String storeLabel,
        String terminalId,
        Boolean isStatic
) {}
