package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.OrderRequest;
import org.example.gundokai.dto.request.UpdateOrderStatusRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.OrderResponse;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.example.gundokai.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    // 1. Tạo đơn hàng
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setMessage("Order created successfully");
        response.setResult(orderService.createOrder(orderRequest));
        return response;
    }


    // 2. Lấy đơn hàng theo ID
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setMessage("Order found");
        response.setResult(orderService.getOrderById(orderId)); // id đã là String
        return response;
    }

    // 3. Lấy đơn hàng theo userId (người dùng)
    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> getOrdersByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<List<OrderResponse>> response = new ApiResponse<>();
        response.setMessage("Orders for user");
        var pageOrders = orderService.getOrdersByUserId(userId, page, size);
        response.setResult(pageOrders.getContent()); // Lấy List<OrderResponse> từ Page<OrderResponse>
        return response;
    }



    // 4. Admin lấy tất cả đơn hàng (có filter trạng thái)
    @GetMapping("/admin")
    public ApiResponse<List<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<List<OrderResponse>> response = new ApiResponse<>();
        response.setMessage("All orders for admin");
        var pageOrders = orderService.getAllOrdersForAdmin(status, page, size);
        response.setResult(pageOrders.getContent());  // Lấy danh sách từ Page
        return response;
    }


    // 5. Admin cập nhật trạng thái đơn hàng
    @PutMapping(value = "/update-status/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setMessage("Order status updated");
        response.setResult(orderService.updateOrderStatus(orderId, request.getNewStatus()));
        return response;
    }


    // 6. Người dùng huỷ đơn
    @PutMapping("/cancel/{orderId}")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable String orderId) {
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setMessage("Order cancelled");
        response.setResult(orderService.cancelOrder(orderId));
        return response;
    }

    // 7. Người dùng thanh toán lại đơn
    @PostMapping("/retry-payment/{orderId}")
    public ApiResponse<OrderResponse> retryPayment(
            @PathVariable String orderId,
            @RequestParam PaymentMethod paymentMethod
    ) {
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setMessage("Payment retried");
        response.setResult(orderService.retryPayment(orderId, paymentMethod));
        return response;
    }

}
