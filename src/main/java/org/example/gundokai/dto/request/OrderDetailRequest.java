package org.example.gundokai.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDetailRequest {
    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
}