package org.example.gundokai.service;

import lombok.RequiredArgsConstructor;
import org.example.gundokai.dto.respone.PaymentLogResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.PaymentLog;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.example.gundokai.enums.PaymentStatus;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.PaymentMapper;
import org.example.gundokai.repository.OrderRepository;
import org.example.gundokai.repository.PaymentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentLogService {
    @Autowired
    private OrderRepository orderRepository;

    private final PaymentLogRepository paymentLogRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentLog createPaymentLog(String orderId, String method, BigDecimal amount) {
        PaymentLog log = new PaymentLog();
        log.setOrderId(orderId);
        log.setMethod(method); // VNPay hoặc COD
        log.setStatus("PENDING"); // Trạng thái mặc định khi tạo mới
        log.setAmount(amount); // Số tiền cần thanh toán

        if ("COD".equalsIgnoreCase(method)) {
            log.setTransactionId(String.valueOf(System.currentTimeMillis())); // chỉ lấy timestamp, không thêm "COD-"
            log.setPaidAt(LocalDateTime.now()); // Đặt thời gian thanh toán là thời điểm hiện tại
        } else {
            log.setTransactionId(null); // VNPay sẽ xử lý transactionId sau
            log.setPaidAt(null); // VNPay sẽ xử lý paidAt sau
        }

        return paymentLogRepository.save(log);
    }


    @Transactional
    public PaymentLog updatePaymentLogOnly(String orderId, String transactionId, String status, LocalDateTime paidAt) {
        // 1. Tìm hoặc tạo mới PaymentLog
        PaymentLog log = paymentLogRepository.findByOrderId(orderId).orElse(null);
        if (log == null) {
            log = new PaymentLog();
            log.setOrderId(orderId);
            log.setMethod("VNPAY");
            log.setAmount(BigDecimal.ZERO); // có thể lấy từ order nếu cần
        }
        log.setTransactionId(transactionId);
        log.setStatus(status);
        log.setPaidAt(paidAt);
        paymentLogRepository.save(log);

        // 2. Cập nhật Order tương ứng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // 3. Cập nhật paymentStatus
        order.setPaymentStatus(PaymentStatus.valueOf(status));

        // 4. Nếu là VNPAY và trạng thái CONFIRMED, cập nhật order.status thành PROCESSING
        if (PaymentStatus.CONFIRMED.name().equalsIgnoreCase(status)
                && order.getPaymentMethod() == PaymentMethod.VNPAY) {
            order.setStatus(OrderStatus.PROCESSING);
        }

        orderRepository.save(order);

        return log;
    }
    @Transactional(readOnly = true)
    public Page<PaymentLogResponse> getPaymentLogsByMethod(String method, Pageable pageable) {
        if ("all".equalsIgnoreCase(method)) {
            // Trả về tất cả các phương thức thanh toán
            return paymentLogRepository.findAll(pageable)
                    .map(paymentLog -> {
                        Order order = orderRepository.findById(paymentLog.getOrderId())
                                .orElse(null); // Lấy thông tin từ Order
                        return paymentMapper.toPaymentLogResponse(paymentLog, order);
                    });
        }
        // Trả về theo phương thức cụ thể
        return paymentLogRepository.findByMethod(method, pageable)
                .map(paymentLog -> {
                    Order order = orderRepository.findById(paymentLog.getOrderId())
                            .orElse(null); // Lấy thông tin từ Order
                    return paymentMapper.toPaymentLogResponse(paymentLog, order);
                });
    }

    @Transactional(readOnly = true)
    public Page<PaymentLogResponse> getPaymentLogsByMethodAndStatus(String method, String status, Pageable pageable) {
        if ("all".equalsIgnoreCase(method)) {
            // Trả về tất cả các phương thức thanh toán với trạng thái cụ thể
            return paymentLogRepository.findByStatus(status, pageable)
                    .map(paymentLog -> {
                        Order order = orderRepository.findById(paymentLog.getOrderId())
                                .orElse(null); // Lấy thông tin từ Order
                        return paymentMapper.toPaymentLogResponse(paymentLog, order);
                    });
        }
        // Trả về theo phương thức và trạng thái cụ thể
        return paymentLogRepository.findByMethodAndStatus(method, status, pageable)
                .map(paymentLog -> {
                    Order order = orderRepository.findById(paymentLog.getOrderId())
                            .orElse(null); // Lấy thông tin từ Order
                    return paymentMapper.toPaymentLogResponse(paymentLog, order);
                });
    }

}