package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.OrderRequest;
import org.example.gundokai.dto.respone.OrderResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.User;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.OrderMapper;
import org.example.gundokai.repository.OrderRepository;
import org.example.gundokai.repository.UserRepository;
import org.example.gundokai.enums.PaymentStatus;
import org.example.gundokai.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class OrderService {

    OrderRepository orderRepository;
    OrderMapper orderMapper;
    UserRepository userRepository;

    public OrderResponse createOrder(OrderRequest request) {
        String userId = SecurityUtil.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Order order = orderMapper.toOrder(request);
        // Bỏ dòng: order.setId(UUID.randomUUID().toString());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());

        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }


    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    public Page<OrderResponse> getOrdersByUser(int page, int size) {
        String userId = SecurityUtil.getUserId();
        Page<Order> orders = orderRepository.findByUser_Id(userId, PageRequest.of(page, size));
        return orders.map(orderMapper::toOrderResponse);
    }

    public Page<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            orders = orderRepository.findAll(PageRequest.of(page, size));
        }
        return orders.map(orderMapper::toOrderResponse);
    }

    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(newStatus);
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

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
            // chuyển String status sang enum OrderStatus
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByStatus(orderStatus, PageRequest.of(page, size));
        } else {
            orders = orderRepository.findAll(PageRequest.of(page, size));
        }
        return orders.map(orderMapper::toOrderResponse);
    }
}
