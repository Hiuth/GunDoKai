package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.dto.request.NotificationsRequest;
import org.example.gundokai.dto.request.OrderDetailRequest;
import org.example.gundokai.dto.request.OrderRequest;
import org.example.gundokai.dto.respone.OrderResponse;
import org.example.gundokai.dto.respone.OrderDetailResponse;
import org.example.gundokai.entity.*;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.example.gundokai.enums.PaymentStatus;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.OrderDetailMapper;
import org.example.gundokai.mapper.OrderMapper;
import org.example.gundokai.repository.*;
import org.example.gundokai.util.SecurityUtil;
import org.example.gundokai.util.VNPayPaymentUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class OrderService {

    OrderRepository orderRepository;
    OrderDetailRepository orderDetailRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    PaymentLogService paymentLogService;
    OrderMapper orderMapper;
    OrderDetailMapper orderDetailMapper;
    NotificationsService notificationService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String userId = SecurityUtil.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // ✅ Kiểm tra stock trước khi tạo order
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderDetailRequest item : request.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));

                // ✅ Kiểm tra stock đủ không
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Sản phẩm " + product.getProductName() + " chỉ còn " + product.getStockQuantity() + " trong kho, bạn đặt " + item.getQuantity());
                }
            }
        }

        // Tạo order
        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        // ✅ Thêm OrderDetails và giảm stock ngay lập tức
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderDetailRequest item : request.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));

                // ✅ Giảm stock ngay khi tạo order (cho cả COD và VNPay)
                int currentStock = product.getStockQuantity();
                int newStock = currentStock - item.getQuantity();
                product.setStockQuantity(newStock);
                productRepository.save(product);

                log.info("Decreased stock for product {} ({}): {} -> {} (Order: {}, Method: {})",
                        product.getId(), product.getProductName(), currentStock, newStock,
                        order.getId(), request.getPaymentMethod());

                OrderDetail detail = orderDetailMapper.toOrderDetail(item);
                detail.setOrder(order);
                detail.setProduct(product);
                detail.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
                detail.setSubTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                order.getOrderDetails().add(detail);

                orderDetailRepository.save(detail);
            }
        }

        // Cập nhật tổng tiền
        order.setTotalAmount(calculateTotalAmount(order.getId()));
        orderRepository.save(order);

        // Lấy lại order đã có orderDetails
        Order savedOrder = orderRepository.findByIdWithDetails(order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderResponse response = orderMapper.toOrderResponse(savedOrder);
        List<OrderDetailResponse> detailResponses = savedOrder.getOrderDetails().stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .collect(Collectors.toList());
        response.setOrderDetails(detailResponses);

        // Xử lý thanh toán
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            // Thanh toán VNPay - stock đã giảm ở trên
            paymentLogService.createPaymentLog(savedOrder.getId(), "VNPAY", savedOrder.getTotalAmount());

            String bankCode = "VNPAYQR";
            String ipAddress = "127.0.0.1";
            String paymentUrl = VNPayPaymentUtil.generateVnpayPaymentUrl(
                    savedOrder.getId(),
                    savedOrder.getTotalAmount(),
                    bankCode,
                    ipAddress
            );

            response.setPaymentUrl(paymentUrl);
            log.info("Initiated VNPAY payment for Order ID: {}, Amount: {}, URL: {}, Stock decreased immediately",
                    savedOrder.getId(), savedOrder.getTotalAmount(), paymentUrl);
        } else if (request.getPaymentMethod() == PaymentMethod.COD) {
            // Thanh toán COD - stock đã giảm ở trên
            paymentLogService.createPaymentLog(savedOrder.getId(), "COD", savedOrder.getTotalAmount());
            log.info("Created COD payment log for Order ID: {}, Amount: {}, Stock decreased immediately",
                    savedOrder.getId(), savedOrder.getTotalAmount());
        }

        return response;
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderResponse response = orderMapper.toOrderResponse(order);
        response.setOrderDetails(order.getOrderDetails().stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .collect(Collectors.toList()));
        return response;
    }

    public Page<OrderResponse> getOrdersByUser(int page, int size) {
        String userId = SecurityUtil.getUserId();
        Page<Order> orders = orderRepository.findByUser_Id(userId, PageRequest.of(page, size));
        return orders.map(orderMapper::toOrderResponse);
    }

    public Page<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Page<Order> orders = (status != null)
                ? orderRepository.findByStatus(status, PageRequest.of(page, size))
                : orderRepository.findAll(PageRequest.of(page, size));

        return orders.map(orderMapper::toOrderResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(newStatus);
        orderRepository.save(order);
        NotificationsRequest notificationRequest = NotificationsRequest.builder()
                .email(order.getEmail())
                .message("Đơn hàng của bạn đã được cập nhật trạng thái: " + newStatus.name())
                .build();
        notificationService.createNotification(notificationRequest);
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        String userId = SecurityUtil.getUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse retryPayment(String orderId, PaymentMethod paymentMethod) {
        String userId = SecurityUtil.getUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getPaymentStatus() != PaymentStatus.FAILED) {
            throw new AppException(ErrorCode.ORDER_CANNOT_REPAY);
        }

        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }
    @Transactional
    public OrderResponse adminCancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        NotificationsRequest notificationRequest = NotificationsRequest.builder()
                .email(order.getEmail())
                .message("Đơn hàng của bạn đã bị hủy")
                .build();
        notificationService.createNotification(notificationRequest);
        return orderMapper.toOrderResponse(order);
    }

    // OrderService.java
    public Page<OrderResponse> getOrdersByUserId(String userId, int page, int size) {
        // ✅ Sử dụng method có sort sẵn
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders = orderRepository.findByUser_IdOrderByOrderDateDesc(userId, pageable);

        log.info("Found {} orders for user {} (sorted by orderDate DESC)", orders.getContent().size(), userId);

        return orders.map(order -> {
            OrderResponse response = orderMapper.toOrderResponse(order);

            // Map order details
            List<OrderDetailResponse> details = order.getOrderDetails().stream()
                    .map(orderDetailMapper::toOrderDetailResponse)
                    .collect(Collectors.toList());
            response.setOrderDetails(details);

            return response;
        });
    }

    public Page<OrderResponse> getAllOrdersForAdmin(String status, int page, int size) {
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByStatus(orderStatus, PageRequest.of(page, size));
        } else {
            orders = orderRepository.findAll(PageRequest.of(page, size));
        }
        return orders.map(orderMapper::toOrderResponse);
    }

    public BigDecimal calculateTotalAmount(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return orderDetailRepository.findByOrder(order).stream()
                .map(OrderDetail::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Transactional
    public void updatePaymentStatus(String orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
    }
}
