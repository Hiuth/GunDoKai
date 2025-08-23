package org.example.gundokai.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.configuration.VnpayConfig;
import org.example.gundokai.service.PaymentProcessorService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    // VNPayCallbackController.java
    // VNPayCallbackController.java
    @GetMapping("/vnpay-return")
    public String handleVnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
            // ✅ Redirect về trang chủ với JavaScript auto-clear cart
            return "redirect:http://localhost:3000/payment-success?status=error&reason=invalid_signature";
        }

        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderId = request.getParameter("vnp_TxnRef");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String payDate = request.getParameter("vnp_PayDate");
        String amount = request.getParameter("vnp_Amount");

        if (payDate == null || payDate.length() != 14) {
            log.error("[VNPAY] Invalid payDate format: {}", payDate);
            return "redirect:http://localhost:3000/payment-success?status=error&reason=invalid_date";
        }

        String status = ("00".equals(transactionStatus) && "00".equals(responseCode)) ? "CONFIRMED" : "FAILED";

        try {
            LocalDateTime paidAt = parsePayDate(payDate);
            log.info("[VNPAY] Processing payment orderId={}, transactionId={}, status={}, paidAt={}",
                    orderId, transactionId, status, paidAt);

            // Process payment
            paymentProcessorService.processPayment(orderId, transactionId, status, paidAt);

            if ("CONFIRMED".equals(status)) {
                log.info("[VNPAY] Payment confirmed for order {}. Using HTML redirect.", orderId);

                String actualAmount = String.valueOf(Long.parseLong(amount) / 100);
                String redirectUrl = "http://localhost:3000/payment-success?status=success&orderId=" + orderId + "&amount=" + actualAmount;

                log.info("[VNPAY] HTML Redirect URL: {}", redirectUrl);

                // ✅ Sử dụng HTML redirect thay vì Spring redirect
                response.setContentType("text/html");
                response.getWriter().write(
                        "<html><head>" +
                                "<meta http-equiv='refresh' content='0; url=" + redirectUrl + "'>" +
                                "<script>window.location.href='" + redirectUrl + "';</script>" +
                                "</head><body>" +
                                "<p>Redirecting to payment success page...</p>" +
                                "<p>If not redirected, <a href='" + redirectUrl + "'>click here</a></p>" +
                                "</body></html>"
                );
                response.getWriter().flush();

                return null; // Không return view vì đã write response

            } else {
                // Similar handling for failed payment
                String redirectUrl = "http://localhost:3000/payment-success?status=failed&orderId=" + orderId;

                response.setContentType("text/html");
                response.getWriter().write(
                        "<html><head>" +
                                "<meta http-equiv='refresh' content='0; url=" + redirectUrl + "'>" +
                                "<script>window.location.href='" + redirectUrl + "';</script>" +
                                "</head><body>" +
                                "<p>Redirecting...</p>" +
                                "</body></html>"
                );
                response.getWriter().flush();

                return null;
            }

        } catch (Exception e) {
            log.error("[VNPAY] Error while processing payment: {}", e.getMessage(), e);

            String redirectUrl = "http://localhost:3000/payment-success?status=error&reason=system_error";

            response.setContentType("text/html");
            response.getWriter().write(
                    "<html><head>" +
                            "<meta http-equiv='refresh' content='0; url=" + redirectUrl + "'>" +
                            "<script>window.location.href='" + redirectUrl + "';</script>" +
                            "</head><body>" +
                            "<p>Error occurred, redirecting...</p>" +
                            "</body></html>"
            );
            response.getWriter().flush();

            return null;
        }
    }

    private LocalDateTime parsePayDate(String payDate) {
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
