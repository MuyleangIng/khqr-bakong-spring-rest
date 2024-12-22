package com.example.demo.service;

import com.example.demo.model.*;
import java.util.List;

public interface KhqrService {
    QrResponse generateQr(PaymentRequest request);
    PaymentStatusResponse checkPayment(String md5Hash);
    List<PaymentTransaction> getPaymentHistory(String bankAccount);
    PaymentTransaction updatePaymentStatus(String md5Hash, String status);
    PaymentTransaction getPaymentByMd5(String md5Hash);
}