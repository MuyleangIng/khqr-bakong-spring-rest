package com.example.demo.service;

import com.example.demo.model.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KhqrServiceImpl implements KhqrService {

    private final PaymentRepository paymentRepository;

    @Value("${app.callback.url}")
    private String callbackBaseUrl;

    private String generateBankDeepLink(String bankType, String md5Hash, Double amount, String currency) {
        // Base URLs for bank apps
        final String ABA_APP_SCHEME = "aba://";
        final String ACLEDA_APP_SCHEME = "acledabank://";

        // Fallback URLs (Play Store)
        final String ABA_PLAYSTORE = "https://play.google.com/store/apps/details?id=com.aba.mobile";
        final String ACLEDA_PLAYSTORE = "https://play.google.com/store/apps/details?id=kh.com.acleda.mobile";

        String callbackUrl = String.format("%s/api/khqr/callback/%s", callbackBaseUrl, md5Hash);
        String encodedCallback = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8);

        switch (bankType.toUpperCase()) {
            case "ABA":
                // ABA deep link format
                return String.format("intent://payment?md5=%s&amount=%s&currency=%s&callback=%s#Intent;" +
                                "scheme=aba;" +
                                "package=com.aba.mobile;" +
                                "S.browser_fallback_url=%s;" +
                                "end",
                        md5Hash,
                        amount,
                        currency,
                        encodedCallback,
                        ABA_PLAYSTORE
                );

            case "ACLEDA":
                // ACLEDA deep link format
                return String.format("intent://qr-payment?data=%s&amount=%s&currency=%s&callback=%s#Intent;" +
                                "scheme=acledabank;" +
                                "package=kh.com.acleda.mobile;" +
                                "S.browser_fallback_url=%s;" +
                                "end",
                        md5Hash,
                        amount,
                        currency,
                        encodedCallback,
                        ACLEDA_PLAYSTORE
                );

            default:
                return null;
        }
    }

    @Override
    @Transactional
    public QrResponse generateQr(PaymentRequest request) {
        try {
            MerchantInfo merchantInfo = createMerchantInfo(request);
            KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

            if (response.getKHQRStatus().getCode() == 0) {
                String qr = response.getData().getQr();
                String md5 = response.getData().getMd5();

                // Generate deep link for bank app
                String deepLink = generateBankDeepLink(
                        request.getBankType(),
                        md5,
                        request.getAmount(),
                        request.getCurrency()
                );

                // Save transaction
                PaymentTransaction transaction = new PaymentTransaction();
                transaction.setMd5Hash(md5);
                transaction.setAmount(request.getAmount());
                transaction.setCurrency(request.getCurrency());
                transaction.setMerchantName(request.getMerchantName());
                transaction.setBankAccount(request.getBankAccount());
                transaction.setBankType(request.getBankType());
                transaction.setStatus("PENDING");
                transaction.setDeepLink(deepLink);

                paymentRepository.save(transaction);

                return new QrResponse(
                        convertQrToBase64(qr),
                        md5,
                        request.getAmount(),
                        request.getMerchantName(),
                        request.getBankAccount(),
                        deepLink,
                        true,
                        "QR generated successfully"
                );

            } else {
                log.error("Error generating QR: {}", response.getKHQRStatus().getMessage());
                return new QrResponse(
                        null, null, request.getAmount(),
                        request.getMerchantName(),
                        request.getBankAccount(),
                        null, false,
                        response.getKHQRStatus().getMessage()
                );
            }
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return new QrResponse(
                    null, null, request.getAmount(),
                    request.getMerchantName(),
                    request.getBankAccount(),
                    null, false,
                    "Error generating QR: " + e.getMessage()
            );
        }
    }

    @Override
    public PaymentStatusResponse checkPayment(String md5Hash) {
        try {
            // First check local database
            PaymentTransaction transaction = paymentRepository.findByMd5Hash(md5Hash)
                    .orElse(null);

            if (transaction != null && "COMPLETED".equals(transaction.getStatus())) {
                return new PaymentStatusResponse(
                        md5Hash,
                        true,
                        "Payment completed"
                );
            }

            // Then verify with KHQR
            KHQRResponse<CRCValidation> response = BakongKHQR.verify(md5Hash);
            boolean isValid = response.getData().isValid();

            if (isValid && transaction != null) {
                transaction.setStatus("COMPLETED");
                paymentRepository.save(transaction);
            }

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

    @Override
    public List<PaymentTransaction> getPaymentHistory(String bankAccount) {
        return paymentRepository.findByBankAccount(bankAccount);
    }

    @Override
    @Transactional
    public PaymentTransaction updatePaymentStatus(String md5Hash, String status) {
        try {
            PaymentTransaction transaction = paymentRepository.findByMd5Hash(md5Hash)
                    .orElse(null);

            if (transaction != null) {
                transaction.setStatus(status);
                return paymentRepository.save(transaction);
            }
            return null;
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            return null;
        }
    }

    @Override
    public PaymentTransaction getPaymentByMd5(String md5Hash) {
        return paymentRepository.findByMd5Hash(md5Hash).orElse(null);
    }



    private String convertQrToBase64(String qrString) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error converting QR to base64", e);
            return null;
        }
    }

    private MerchantInfo createMerchantInfo(PaymentRequest request) {
        MerchantInfo merchantInfo = new MerchantInfo();

        // Required fields
        merchantInfo.setMerchantId("MERCHANT123");
        merchantInfo.setAcquiringBank(request.getBankType());
        merchantInfo.setBakongAccountId(request.getBankAccount());

        // Business details
        merchantInfo.setMerchantName(request.getMerchantName());
        merchantInfo.setMerchantCity("Phnom Penh");
        merchantInfo.setAmount(request.getAmount());
        merchantInfo.setCurrency(KHQRCurrency.valueOf(request.getCurrency()));

        // Optional fields
        merchantInfo.setStoreLabel(request.getStoreLabel());
        merchantInfo.setTerminalLabel(request.getTerminalId());

        return merchantInfo;
    }
}