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
    private final SocketService socketService;

    @Transactional
    public void processPayment(String orderId, String transactionId, String status, LocalDateTime paidAt) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            log.info("Processing payment result from gateway - orderId: {}, transactionId: {}, status: {}, paidAt: {}",
                    orderId, transactionId, status, paidAt);

            paymentLogService.updatePaymentLogOnly(orderId, transactionId, status, paidAt);
            orderService.updatePaymentStatus(orderId, paymentStatus);

            // ✅ Emit qua WebSocket cho payment result
            socketService.emitPaymentResult(orderId, paymentStatus.name());

            // ✅ THÊM: Emit notification cho admin panel nếu thanh toán thành công
            if (paymentStatus == PaymentStatus.CONFIRMED) {
                // Lấy thông tin order để gửi notification
                var order = orderService.getOrderById(orderId); // Cần implement method này
                if (order != null) {
                    socketService.emitNotification(
                            orderId,
                            order.getCustomerName(),
                            order.getEmail(),
                            order.getTotalAmount(),
                            "VNPay", // hoặc lấy từ payment method thực tế
                            transactionId
                    );
                    log.info("Notification sent for successful payment: {}", orderId);
                }
            }

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

        // ✅ THÊM: Emit notification cho manual payment
        var order = orderService.getOrderById(orderId);
        if (order != null) {
            socketService.emitNotification(
                    orderId,
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getTotalAmount(),
                    "MANUAL", // Đánh dấu là thanh toán thủ công
                    transactionId
            );
            log.info("Manual payment notification sent for order: {}", orderId);
        }
    }

    @Transactional
    public void markOrderAsFailed(String orderId) {
        String transactionId = "sandbox-" + System.currentTimeMillis();
        LocalDateTime paidAt = LocalDateTime.now();
        log.info("Manually marking order as FAILED - orderId: {}, transactionId: {}, paidAt: {}", orderId, transactionId, paidAt);

        paymentLogService.updatePaymentLogOnly(orderId, transactionId, "FAILED", paidAt);
        orderService.updatePaymentStatus(orderId, PaymentStatus.FAILED);

        // ✅ Emit WebSocket cho failed payment
        socketService.emitPaymentResult(orderId, "FAILED");
    }
}
