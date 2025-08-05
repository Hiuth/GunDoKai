package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.OrderDetailRequest;
import org.example.gundokai.dto.request.OrderRequest;
import org.example.gundokai.dto.respone.OrderDetailResponse;
import org.example.gundokai.dto.respone.OrderResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.OrderDetail;
import org.example.gundokai.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "status", constant = "PENDING"),
            @Mapping(target = "user", ignore = true),
            @Mapping(source = "total", target = "totalAmount"),
            @Mapping(source = "customerName", target = "customerName"),
            @Mapping(source = "phoneNumber", target = "phoneNumber"),
            @Mapping(source = "address", target = "address"),
            @Mapping(target = "orderDetails", ignore = true)
    })
    Order toOrder(OrderRequest orderRequest);

    @Mappings({
            @Mapping(source = "id", target = "orderId"),
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "customerName", target = "customerName"),
            @Mapping(source = "phoneNumber", target = "phoneNumber"),
            @Mapping(source = "address", target = "address"),
            @Mapping(source = "orderDate", target = "orderDate"),
            @Mapping(source = "totalAmount", target = "totalAmount"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "paymentMethod", target = "paymentMethod"),
            @Mapping(source = "paymentStatus", target = "paymentStatus"),
            @Mapping(source = "orderDetails", target = "orderDetails")
    })
    OrderResponse toOrderResponse(Order order);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "order", ignore = true),
            @Mapping(target = "product", ignore = true),
            @Mapping(source = "quantity", target = "quantity"),
            @Mapping(target = "unitPrice", ignore = true),
            @Mapping(target = "subTotal", ignore = true)
    })
    OrderDetail toOrderDetail(OrderDetailRequest request, @MappingTarget OrderDetail detail);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "order.id", target = "orderId"),
            @Mapping(source = "product.id", target = "productId"),
            @Mapping(source = "quantity", target = "quantity"),
            @Mapping(source = "unitPrice", target = "unitPrice"),
            @Mapping(source = "subTotal", target = "subTotal")
    })
    OrderDetailResponse toOrderDetailResponse(OrderDetail detail);

    List<OrderDetail> toOrderDetailList(List<OrderDetailRequest> items);
}