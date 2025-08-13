package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.OrderDetailRequest;
import org.example.gundokai.dto.respone.OrderDetailResponse;
import org.example.gundokai.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "quantity", target = "quantity") // Ánh xạ quantity
    @Mapping(target = "unitPrice", ignore = true) // Gán trong service
    @Mapping(target = "subTotal", ignore = true) // Tính trong service
    OrderDetail toOrderDetail(OrderDetailRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "subTotal", target = "subTotal")
    OrderDetailResponse toOrderDetailResponse(OrderDetail detail);
}