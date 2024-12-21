package com.example.demo.service;

import com.example.demo.model.PaymentRequest;
import com.example.demo.model.PaymentStatusResponse;
import com.example.demo.model.QrResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
public class KhqrService {

    public QrResponse generateQr(PaymentRequest request) {
        try {
            MerchantInfo merchantInfo = createMerchantInfo(request);
            log.info("Generating QR with merchant info: {}", merchantInfo);

            KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

            if (response.getKHQRStatus().getCode() == 0) {
                String qr = response.getData().getQr();
                String md5 = response.getData().getMd5();
                log.info("Generated QR successfully: {}", qr);

                return new QrResponse(
                        convertQrToBase64(qr),
                        md5,
                        request.amount(),
                        request.merchantName(),
                        request.bankAccount(),
                        true,
                        "QR generated successfully"
                );
            } else {
                log.error("Error generating QR: {}", response.getKHQRStatus().getMessage());
                return new QrResponse(
                        null,
                        null,
                        request.amount(),
                        request.merchantName(),
                        request.bankAccount(),
                        false,
                        response.getKHQRStatus().getMessage()
                );
            }
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return new QrResponse(
                    null,
                    null,
                    request.amount(),
                    request.merchantName(),
                    request.bankAccount(),
                    false,
                    "Error generating QR: " + e.getMessage()
            );
        }
    }

    private String convertQrToBase64(String qrString) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Error converting QR to base64", e);
            return null;
        }
    }
    public PaymentStatusResponse checkPayment(String md5Hash) {
        try {
            KHQRResponse<CRCValidation> response = BakongKHQR.verify(md5Hash);
            boolean isValid = response.getData().isValid();

            return new PaymentStatusResponse(
                    md5Hash,
                    isValid,
                    isValid ? "Payment verified" : "Payment not verified"
            );
        } catch (Exception e) {
            log.error("Error checking payment", e);
            return new PaymentStatusResponse(
                    md5Hash,
                    false,
                    "Error checking payment: " + e.getMessage()
            );
        }
    }

    private MerchantInfo createMerchantInfo(PaymentRequest request) {
        MerchantInfo merchantInfo = new MerchantInfo();

        // Required fields
        merchantInfo.setMerchantId("MERCHANT123");
        merchantInfo.setAcquiringBank("ACLEDA");
        merchantInfo.setBakongAccountId(request.bankAccount());

        // Business details
        merchantInfo.setMerchantName(request.merchantName());
        merchantInfo.setMerchantCity("Phnom Penh");
        merchantInfo.setAmount(request.amount());
        merchantInfo.setCurrency(KHQRCurrency.valueOf(request.currency()));

        // Optional fields
        merchantInfo.setStoreLabel(request.storeLabel());
        merchantInfo.setTerminalLabel(request.terminalId());

        return merchantInfo;
    }
}
