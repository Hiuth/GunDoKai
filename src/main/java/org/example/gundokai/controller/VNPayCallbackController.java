package org.example.gundokai.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.configuration.VnpayConfig;
import org.example.gundokai.service.PaymentProcessorService;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

import static org.example.gundokai.configuration.VnpayConfig.hmacSHA512;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VNPayCallbackController {

    private final PaymentProcessorService paymentProcessorService;

    @GetMapping("/vnpay-return")
    public String handleVnpayReturn(HttpServletRequest request) {
        log.info("[VNPAY] Handling return callback...");

        Map<String, String> fields = new HashMap<>();
        Map<String, String[]> paramMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                fields.put(key, value);
            }
        }

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = fields.get(key);
            // Bỏ encode hoặc dùng UTF-8
            // value = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

            if (i > 0) {
                hashData.append("&");
            }
            hashData.append(key).append("=").append(value);
        }

        log.info("[VNPAY] Data to hash: {}", hashData.toString());

        String secureHashCheck = hmacSHA512(VnpayConfig.VNP_HASH_SECRET, hashData.toString());
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        if (!secureHashCheck.equalsIgnoreCase(vnp_SecureHash)) {
            log.warn("[VNPAY] Invalid signature. Calculated: {}, Received: {}", secureHashCheck, vnp_SecureHash);
            return "invalid-signature";
        }

        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderId = request.getParameter("vnp_TxnRef");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String payDate = request.getParameter("vnp_PayDate");

        log.info("[VNPAY] Received payDate: {}", payDate);

        if (payDate == null || payDate.length() != 14) {
            log.error("[VNPAY] Invalid payDate format: {}", payDate);
            return "payment-error";
        }

        String status = ("00".equals(transactionStatus) && "00".equals(responseCode)) ? "CONFIRMED" : "FAILED";

        try {
            LocalDateTime paidAt = parsePayDate(payDate);
            log.info("[VNPAY] Processing payment orderId={}, transactionId={}, status={}, paidAt={}", orderId, transactionId, status, paidAt);

            paymentProcessorService.processPayment(orderId, transactionId, status, paidAt);
            return "payment-" + status.toLowerCase();
        } catch (Exception e) {
            log.error("[VNPAY] Error while processing payment: {}", e.getMessage(), e);
            return "payment-error";
        }
    }


    private LocalDateTime parsePayDate(String payDate) {
        // vnp_PayDate có format: yyyyMMddHHmmss, ví dụ: 20250805155315
        return LocalDateTime.of(
                Integer.parseInt(payDate.substring(0, 4)),      // year
                Integer.parseInt(payDate.substring(4, 6)),      // month
                Integer.parseInt(payDate.substring(6, 8)),      // day
                Integer.parseInt(payDate.substring(8, 10)),     // hour
                Integer.parseInt(payDate.substring(10, 12)),    // minute
                Integer.parseInt(payDate.substring(12, 14))     // second
        );
    }
}
