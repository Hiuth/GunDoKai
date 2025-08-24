package org.example.gundokai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.dto.request.NotificationsRequest;
import org.example.gundokai.entity.Notifications;
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
    private final NotificationsService notificationService;

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
                    // ✅ THÊM: Tạo thông báo cho user sau khi thanh toán thành công
                    NotificationsRequest notificationRequest = NotificationsRequest.builder()
                            .email(order.getEmail())
                            .message(String.format("Thanh toán thành công cho đơn hàng #%s. Số tiền: %,.0f VND. Mã giao dịch: %s", 
                                    orderId, order.getTotalAmount(), transactionId))
                            .build();
                    notificationService.createNotification(notificationRequest);

                    NotificationsRequest notificationRequest2 = NotificationsRequest.builder()
                            .email("admin")
                            .message(String.format("Bạn có đơn hàng mới #%s. Số tiền: %,.0f VND. Hãy kiểm tra ngay",
                                    orderId, order.getTotalAmount()))
                            .build();

                    notificationService.createNotification(notificationRequest2);
                    log.info("Payment success notification created for user: {} - orderId: {}", order.getEmail(), orderId);
                    
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
            
            // ✅ THÊM: Tạo thông báo cho user sau khi thanh toán thủ công thành công
            NotificationsRequest notificationRequest = NotificationsRequest.builder()
                    .email(order.getEmail())
                    .message(String.format("Thanh toán thủ công thành công cho đơn hàng #%s. Số tiền: %,.0f VND. Mã giao dịch: %s", 
                            orderId, order.getTotalAmount(), transactionId))
                    .build();
            notificationService.createNotification(notificationRequest);

            NotificationsRequest notificationRequest2 = NotificationsRequest.builder()
                            .email("admin")
                            .message(String.format("Bạn có đơn hàng mới #%s. Số tiền: %,.0f VND. Hãy kiểm tra ngay",
                                    orderId, order.getTotalAmount()))
                            .build();

                    notificationService.createNotification(notificationRequest2);
            log.info("Manual payment notification created for user: {} - orderId: {}", order.getEmail(), orderId);
            
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
        
        // ✅ THÊM: Tạo thông báo cho user khi thanh toán thất bại
        var order = orderService.getOrderById(orderId);
        if (order != null) {
            NotificationsRequest notificationRequest = NotificationsRequest.builder()
                    .email(order.getEmail())
                    .message(String.format("Thanh toán thất bại cho đơn hàng #%s. Vui lòng thử lại hoặc liên hệ hỗ trợ.", orderId))
                    .build();
            
            notificationService.createNotification(notificationRequest);
            log.info("Payment failure notification created for user: {} - orderId: {}", order.getEmail(), orderId);
        }
    }
}
