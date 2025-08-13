package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class OrderService {

    OrderRepository orderRepository;
    OrderDetailRepository orderDetailRepository;
    ProductRepository productRepository;
    UserRepository userRepository;

    OrderMapper orderMapper;
    OrderDetailMapper orderDetailMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String userId = SecurityUtil.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tạo order
        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());

        orderRepository.save(order);

        // Thêm OrderDetails nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderDetailRequest item : request.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));

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

    public Page<OrderResponse> getOrdersByUserId(String userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUser_Id(userId, PageRequest.of(page, size));
        return orders.map(orderMapper::toOrderResponse);
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
}
