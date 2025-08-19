package org.example.gundokai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.enums.PaymentStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessorService {
    private final PaymentLogService paymentLogService;
    private final OrderService orderService;

    @Transactional
    public void processPayment(String orderId, String transactionId, String status, LocalDateTime paidAt) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            log.info("Processing payment result from gateway - orderId: {}, transactionId: {}, status: {}, paidAt: {}",
                    orderId, transactionId, status, paidAt);

            paymentLogService.updatePaymentLogOnly(orderId, transactionId, status, paidAt);
            orderService.updatePaymentStatus(orderId, paymentStatus);

            log.info("Payment status updated successfully for orderId: {}", orderId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment status received from gateway: {}", status);
            throw new RuntimeException("Invalid payment status received from gateway: " + status);
        }
    }

    @Transactional
    public void markOrderAsPaid(String orderId) {
        String transactionId = "sandbox-" + System.currentTimeMillis();
        LocalDateTime paidAt = LocalDateTime.now();
        log.info("Manually marking order as PAID - orderId: {}, transactionId: {}, paidAt: {}", orderId, transactionId, paidAt);

        paymentLogService.updatePaymentLogOnly(orderId, transactionId, "CONFIRMED", paidAt);
        orderService.updatePaymentStatus(orderId, PaymentStatus.CONFIRMED);
    }

    @Transactional
    public void markOrderAsFailed(String orderId) {
        String transactionId = "sandbox-" + System.currentTimeMillis();
        LocalDateTime paidAt = LocalDateTime.now();
        log.info("Manually marking order as FAILED - orderId: {}, transactionId: {}, paidAt: {}", orderId, transactionId, paidAt);

        paymentLogService.updatePaymentLogOnly(orderId, transactionId, "FAILED", paidAt);
        orderService.updatePaymentStatus(orderId, PaymentStatus.FAILED);
    }
}
