package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.OrderDetailRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.OrderDetailResponse;
import org.example.gundokai.service.OrderDetailService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-detail")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailController {

    OrderDetailService orderDetailService;

    // 1. Tạo chi tiết đơn hàng (yêu cầu orderId trong URL)
    @PostMapping(value = "/{orderId}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<OrderDetailResponse> createOrderDetail(
            @PathVariable String orderId,
            @RequestBody OrderDetailRequest request
    ) {
        request.setOrderId(orderId); // Gán orderId từ URL
        ApiResponse<OrderDetailResponse> response = new ApiResponse<>();
        response.setMessage("Order detail created successfully");
        response.setResult(orderDetailService.addOrderDetail(request)); // Sử dụng addOrderDetail thay vì create
        return response;
    }

    // 2. Lấy chi tiết đơn hàng theo ID (yêu cầu orderId trong URL)
    @GetMapping("/{orderId}/{detailId}")
    public ApiResponse<OrderDetailResponse> getOrderDetailById(
            @PathVariable String orderId,
            @PathVariable String detailId
    ) {
        // Kiểm tra orderId (tùy chọn, đã xử lý trong service)
        orderDetailService.getByOrderId(orderId); // Kiểm tra tồn tại
        ApiResponse<OrderDetailResponse> response = new ApiResponse<>();
        response.setMessage("Order detail found");
        response.setResult(orderDetailService.getOrderDetail(detailId));
        return response;
    }

    // 3. Lấy tất cả chi tiết của một đơn hàng
    @GetMapping("/{orderId}")
    public ApiResponse<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable String orderId) {
        ApiResponse<List<OrderDetailResponse>> response = new ApiResponse<>();
        response.setMessage("Order details retrieved successfully");
        response.setResult(orderDetailService.getByOrderId(orderId));
        return response;
    }

    // 4. Cập nhật chi tiết đơn hàng (yêu cầu orderId trong URL)
    @PutMapping(value = "/{orderId}/update/{detailId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<OrderDetailResponse> updateOrderDetail(
            @PathVariable String orderId,
            @PathVariable String detailId,
            @RequestBody OrderDetailRequest request
    ) {
        request.setOrderId(orderId); // Gán orderId từ URL
        ApiResponse<OrderDetailResponse> response = new ApiResponse<>();
        response.setMessage("Order detail updated successfully");
        response.setResult(orderDetailService.updateOrderDetail(detailId, request));
        return response;
    }

    // 5. Xoá chi tiết đơn hàng (yêu cầu orderId trong URL)
    @DeleteMapping("/{orderId}/delete/{detailId}")
    public ApiResponse<Void> deleteOrderDetail(
            @PathVariable String orderId,
            @PathVariable String detailId
    ) {
        // Kiểm tra orderId (tùy chọn, đã xử lý trong service)
        orderDetailService.getByOrderId(orderId); // Kiểm tra tồn tại
        orderDetailService.deleteOrderDetail(detailId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Order detail deleted successfully");
        return response;
    }
}