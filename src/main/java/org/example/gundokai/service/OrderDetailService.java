package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.OrderDetailRequest;
import org.example.gundokai.dto.respone.OrderDetailResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.OrderDetail;
import org.example.gundokai.entity.Product;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.OrderDetailMapper;
import org.example.gundokai.repository.OrderDetailRepository;
import org.example.gundokai.repository.OrderRepository;
import org.example.gundokai.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailService {
    OrderDetailRepository orderDetailRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;
    OrderDetailMapper orderDetailMapper;

    @Transactional
    public OrderDetailResponse addOrderDetail(OrderDetailRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));

        OrderDetail detail = orderDetailMapper.toOrderDetail(request);
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setSubTotal(
                BigDecimal.valueOf(product.getPrice())
                        .multiply(BigDecimal.valueOf(request.getQuantity()))
        );

        orderDetailRepository.save(detail);
        return orderDetailMapper.toOrderDetailResponse(detail);
    }

    @Transactional
    public void deleteOrderDetail(String detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        orderDetailRepository.delete(detail);
    }

    @Transactional
    public OrderDetailResponse updateOrderDetail(String detailId, OrderDetailRequest request) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));

        // Cập nhật product
        detail.setProduct(product);

        // Cập nhật quantity
        detail.setQuantity(request.getQuantity());

        // Cập nhật unitPrice dựa trên price từ request (ưu tiên) hoặc product.getPrice()
        BigDecimal unitPrice = (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0)
                ? request.getPrice()
                : BigDecimal.valueOf(product.getPrice());
        detail.setUnitPrice(unitPrice);

        // Cập nhật subTotal
        detail.setSubTotal(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())));

        orderDetailRepository.save(detail);
        return orderDetailMapper.toOrderDetailResponse(detail);
    }

    public OrderDetailResponse getOrderDetail(String detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        return orderDetailMapper.toOrderDetailResponse(detail);
    }
    public List<OrderDetailResponse> getByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        return details.stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .toList();
    }

}