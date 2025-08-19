package org.example.gundokai.controller;
import lombok.RequiredArgsConstructor;
import org.example.gundokai.dto.respone.PaymentLogResponse;
import org.example.gundokai.service.PaymentLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment-logs")
public class PaymentLogController {

    private final PaymentLogService paymentLogService;

    @GetMapping
    public Page<PaymentLogResponse> getPaymentLogs(
            @RequestParam String method, // COD, VNPay hoặc all
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentLogService.getPaymentLogsByMethod(method, pageable);
    }

    @GetMapping("/filter")
    public Page<PaymentLogResponse> getPaymentLogsByMethodAndStatus(
            @RequestParam String method, // COD, VNPay hoặc all
            @RequestParam String status, // CONFIRMED, FAILED, PENDING
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentLogService.getPaymentLogsByMethodAndStatus(method, status, pageable);
    }
}