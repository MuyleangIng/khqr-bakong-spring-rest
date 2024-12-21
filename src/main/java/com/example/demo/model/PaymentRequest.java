package com.example.demo.model;

import lombok.Data;
import jakarta.validation.constraints.Positive;

@Data
public class PaymentRequest {
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}