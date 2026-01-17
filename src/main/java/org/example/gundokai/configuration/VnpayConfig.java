package org.example.gundokai.configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VnpayConfig {

    // Thông tin từ email VNPay
    public static final String VNP_TMN_CODE = "2QD9WIPZ";
    public static final String VNP_HASH_SECRET = "C11IGDGVIVV21157LRRKOM6APJ1BFHSC";
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // URL return của bạn
    public static final String VNP_RETURN_URL = "https://ec2-18-142-161-159.ap-southeast-1.compute.amazonaws.com/vnpay-return";

    // Có thể thêm IPN URL nếu cần
    public static final String VNP_IPN_URL = "https://ec2-18-142-161-159.ap-southeast-1.compute.amazonaws.com/vnpay-ipn";

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}