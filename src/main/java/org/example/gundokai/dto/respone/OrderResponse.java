package org.example.gundokai.dto.respone;
import lombok.Builder;
import lombok.Data;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.example.gundokai.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class OrderResponse {
    private String orderId;
    private String userId;
    private String customerName;
    private String phoneNumber;
    private String address;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private List<OrderDetailResponse> orderDetails;
    private String paymentUrl;

}