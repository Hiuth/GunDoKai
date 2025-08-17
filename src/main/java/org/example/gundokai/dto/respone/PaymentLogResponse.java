package org.example.gundokai.dto.respone;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentLogResponse {
    private String id;
    private String orderId;
    private String customerName; // Nếu cần thêm thông tin khách hàng từ Order
    private String customerEmail; // Nếu cần thêm thông tin email từ Order
    private String method; // VNPay hoặc COD
    private String status; // CONFIRMED, FAILED, PENDING
    private String transactionId; // Mã giao dịch từ VNPay
    private BigDecimal amount; // Số tiền cần thanh toán
    private LocalDateTime paidAt; // Thời gian thanh toán
    private String phoneNumber; //
}