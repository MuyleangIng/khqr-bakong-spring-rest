package com.example.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KhqrResponse {
    private String qrData;
    private String qrImage;
    private String deeplink;
    private String md5;
    private String transactionId;
}