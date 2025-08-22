package org.example.gundokai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void emitPaymentResult(String orderId, String status) {
        String destination = "/topic/payment/" + orderId;
        log.info("Emitting WebSocket message to {} with status={}", destination, status);
        messagingTemplate.convertAndSend(destination, status);
    }

    // SỬA: Đổi Double thành BigDecimal
    public void emitNotification(String orderId, String customerName, String customerEmail, BigDecimal amount, String method, String transactionId) {
        String destination = "/topic/notifications";

        // Tạo notification object
        NotificationData notification = NotificationData.builder()
                .orderId(orderId)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .amount(amount.doubleValue()) // Convert BigDecimal to Double for JSON
                .method(method)
                .transactionId(transactionId)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Emitting notification to {} for order {}", destination, orderId);
        messagingTemplate.convertAndSend(destination, notification);
    }

    // Inner class cho notification data
    @lombok.Data
    @lombok.Builder
    public static class NotificationData {
        private String orderId;
        private String customerName;
        private String customerEmail;
        private Double amount; // Giữ Double cho JSON serialization
        private String method;
        private String transactionId;
        private Long timestamp;
    }
}