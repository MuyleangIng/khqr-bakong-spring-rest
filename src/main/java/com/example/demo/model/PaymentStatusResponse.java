package com.example.demo.model;

public record PaymentStatusResponse(
        String md5Hash,
        boolean isPaid,
        String message
) {}