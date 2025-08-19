package org.example.gundokai.mapper;

import org.example.gundokai.dto.respone.PaymentLogResponse;
import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.PaymentLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


import org.example.gundokai.dto.respone.PaymentLogResponse;
import org.example.gundokai.entity.PaymentLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "paymentLog.id", target = "id")
    @Mapping(source = "paymentLog.orderId", target = "orderId")
    @Mapping(source = "paymentLog.method", target = "method")
    @Mapping(source = "paymentLog.status", target = "status")
    @Mapping(source = "paymentLog.transactionId", target = "transactionId")
    @Mapping(source = "paymentLog.amount", target = "amount")
    @Mapping(source = "paymentLog.paidAt", target = "paidAt")
    @Mapping(expression = "java(order != null ? order.getCustomerName() : \"Không có tên khách hàng\")", target = "customerName")
    @Mapping(expression = "java(order != null ? order.getEmail() : \"Không có email\")", target = "customerEmail") // Sử dụng order.getEmail()
    @Mapping(expression = "java(order != null ? order.getPhoneNumber() : \"Không có số điện thoại\")", target = "phoneNumber")
    PaymentLogResponse toPaymentLogResponse(PaymentLog paymentLog, Order order);
}