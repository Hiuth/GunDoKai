package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.OrderRequest;
import org.example.gundokai.dto.respone.OrderResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

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
            @Mapping(source = "address", target = "address")
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
            @Mapping(source = "paymentMethod", target = "paymentMethod")
    })
    OrderResponse toOrderResponse(Order order);
}

