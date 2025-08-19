package org.example.gundokai.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.util.VNPayPaymentUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RedirectToVnpayController {

    @GetMapping("/redirect-to-vnpay")
    public void redirectToVnpay(HttpServletResponse response) throws IOException {
        // Dữ liệu test
        String orderId = "ORDER123";
        BigDecimal amount = BigDecimal.valueOf(100000); // 100k VND
        String bankCode = "VNPAYQR";
        String ipAddress = "127.0.0.1";

        // Tạo URL thanh toán VNPAY
        String paymentUrl = VNPayPaymentUtil.generateVnpayPaymentUrl(
                orderId,
                amount,
                bankCode,
                ipAddress
        );

        // Redirect sang trang thanh toán
        response.sendRedirect(paymentUrl);
    }
}
