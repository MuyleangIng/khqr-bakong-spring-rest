package com.example.demo.model;

public record QrResponse(
        String qrData,     // Changed from 'qr' to 'qrData' to match frontend
        String md5Hash,    // Renamed to match frontend
        double amount,
        String merchantName,
        String bankAccount,
        boolean success,
        String message
) {}