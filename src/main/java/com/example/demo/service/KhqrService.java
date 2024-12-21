package com.example.demo.service;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KhqrService {

    public String generateQr() {
        try {
            MerchantInfo merchantInfo = createMerchantInfo();
            log.info("Generating QR with merchant info: {}", merchantInfo);

            KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

            if (response.getKHQRStatus().getCode() == 0) {
                String qr = response.getData().getQr();
                log.info("Generated QR successfully: {}", qr);
                return qr;
            } else {
                log.error("Error generating QR: {}", response.getKHQRStatus().getMessage());
                throw new RuntimeException("Failed to generate QR: " + response.getKHQRStatus().getMessage());
            }
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private MerchantInfo createMerchantInfo() {
        MerchantInfo merchantInfo = new MerchantInfo();

        // Required fields
        merchantInfo.setMerchantId("MERCHANT123");
        merchantInfo.setAcquiringBank("ACLEDA");
        merchantInfo.setBakongAccountId("proeung_chiso@aclb");

        // Business details
        merchantInfo.setMerchantName("istad shop");
        merchantInfo.setMerchantCity("Phnom Penh");
        merchantInfo.setAmount(500.0);
        merchantInfo.setCurrency(KHQRCurrency.KHR);

        // Optional fields
        merchantInfo.setStoreLabel("istad shop");
        merchantInfo.setMobileNumber("855967920804");
        merchantInfo.setBillNumber("TRX123456");
        merchantInfo.setTerminalLabel("POS-03");

        return merchantInfo;
    }

    public String generateMd5(String qrData) {
        try {
            MerchantInfo merchantInfo = createMerchantInfo();
            KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

            if (response.getKHQRStatus().getCode() == 0) {
                return response.getData().getMd5();
            }
            throw new RuntimeException("Failed to generate MD5");
        } catch (Exception e) {
            log.error("Error generating MD5", e);
            throw new RuntimeException("Failed to generate MD5", e);
        }
    }

    public boolean checkPayment(String qrData) {
        KHQRResponse<CRCValidation> response = BakongKHQR.verify(qrData);
        return response.getData().isValid();
    }
}