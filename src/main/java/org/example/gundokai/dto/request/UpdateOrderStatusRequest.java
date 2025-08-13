package org.example.gundokai.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.gundokai.enums.OrderStatus;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    private OrderStatus newStatus;
}