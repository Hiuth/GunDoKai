package org.example.gundokai.dto.request;
import org.example.gundokai.enums.PaymentMethod;
import lombok.AllArgsConstructor;// tạo ra 1 object chứa trường giá trị mà mình tự nhập data static vô
import lombok.Data; // tự động tạo ra getter,setter,
import lombok.NoArgsConstructor;// tạo ra 1 object rỗng, để khi có request users nó gọi hàm setter để điền trường dữu liệu vào object rỗng đó
import lombok.Builder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private String phoneNumber;
    private String address;
    private String customerName;
}
