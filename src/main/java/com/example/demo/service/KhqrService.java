package com.example.demo.service;

import com.example.demo.model.PaymentRequest;
import com.example.demo.model.PaymentStatusResponse;
import com.example.demo.model.QrResponse;

public interface KhqrService {
    /**
     * Generates a QR code for a payment request
     *
     * @param request Payment request details
     * @return QR Response with generated QR code and metadata
     */
    QrResponse generateQr(PaymentRequest request);

    /**
     * Checks the payment status for a given MD5 hash
     *
     * @param md5Hash MD5 hash of the payment
     * @return Payment status response
     */
    PaymentStatusResponse checkPayment(String md5Hash);
}