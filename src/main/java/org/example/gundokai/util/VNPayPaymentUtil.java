package org.example.gundokai.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VNPayPaymentUtil {

    private static final String VNP_TMN_CODE = "ZM6O5WI1";
    private static final String VNP_HASH_SECRET = "ZGZWSKEV0U2H7EUEWK0FF31UZGEIQPOQ";
    private static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL = "https://86b2e39aa1f9.ngrok-free.app/vnpay-return";

    public static String generateVnpayPaymentUrl(String orderId, BigDecimal amount, String bankCode, String ipAddress) {
        System.out.println("=== GENERATING VNPAY URL ===");

        try {
            // 1. Tạo parameters - QUAN TRỌNG: phải sort theo alphabet
            Map<String, String> vnpParams = new TreeMap<>();

            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNP_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(amount.longValue() * 100)); // VNPay yêu cầu x100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", "payorder" + orderId);
            vnpParams.put("vnp_OrderType", "billpayment");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", VNP_RETURN_URL);
            vnpParams.put("vnp_IpAddr", ipAddress);

            // Thời gian tạo và hết hạn
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plusMinutes(15);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            vnpParams.put("vnp_CreateDate", now.format(formatter));
            vnpParams.put("vnp_ExpireDate", expireTime.format(formatter));

            // 2. Tạo hash data (KHÔNG có vnp_SecureHash)
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                String encodedValue;
                try {
                    encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString())
                            .replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Encoding error: " + e.getMessage());
                    continue; // skip nếu lỗi
                }

                if (hashData.length() > 0) {
                    hashData.append("&");
                    query.append("&");
                }

                // ✅ Đã encode value cho hashData (chính xác)
                hashData.append(entry.getKey()).append("=").append(encodedValue);

                // ✅ Query cũng dùng encodedValue
                query.append(entry.getKey()).append("=").append(encodedValue);
            }


            // 3. Tạo secure hash
            String secureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());

            // 4. Tạo final URL
            String finalUrl = VNP_PAY_URL + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;

            // Debug logs
            System.out.println("HashData: " + hashData.toString());
            System.out.println("SecureHash: " + secureHash);
            System.out.println("Final URL: " + finalUrl);
            System.out.println("===================================");

            // Test hash với data từ log


            return finalUrl;

        } catch (Exception e) {
            System.err.println("Error generating VNPay URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Test với data từ log của bạn

    // Phương thức verify response từ VNPay
    public static boolean verifyVNPayResponse(Map<String, String> params) {
        // Lấy hash từ response
        String receivedHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash"); // Loại bỏ hash khỏi params
        params.remove("vnp_SecureHashType"); // Loại bỏ nếu có

        // Sắp xếp params theo alphabet
        Map<String, String> sortedParams = new TreeMap<>(params);

        // Tạo hash data
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // Tạo hash để so sánh
        String calculatedHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());

        System.out.println("=== VERIFY VNPAY RESPONSE ===");
        System.out.println("Received Hash: " + receivedHash);
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("Hash Data: " + hashData.toString());
        System.out.println("Valid: " + calculatedHash.equals(receivedHash));

        return calculatedHash.equals(receivedHash);
    }

    // HMAC SHA512
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key or data cannot be null");
            }

            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);

            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (Exception ex) {
            System.err.println("HMAC SHA512 Error: " + ex.getMessage());
            ex.printStackTrace();
            return "";
        }
    }

    // Alternative method với SHA256 nếu SHA512 không work
    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);

            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (Exception ex) {
            System.err.println("HMAC SHA256 Error: " + ex.getMessage());
            return "";
        }
    }
}