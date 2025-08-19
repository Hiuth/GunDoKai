//package org.example.gundokai.util;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.FileSystems;
//import java.util.HashMap;
//import java.util.Map;
//
//public class GenerateQRCode {
//
//    public static void generateQRCodeImage(String text, int width, int height, String filePath) throws IOException, com.google.zxing.WriterException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
//
//        String path = FileSystems.getDefault().getPath(filePath).toString();
//        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", new File(path).toPath());
//        System.out.println("QR Code generated successfully at " + filePath);
//    }
//
//    public static void main(String[] args) {
//        try {
//            // Kiểm tra số lượng tham số tối thiểu
//            if (args.length < 5) {
//                throw new IllegalArgumentException("Error: Missing required parameters. Please provide: customerName, phoneNumber, address, productId, and quantity.");
//            }
//
//            // Sử dụng RestTemplate để gọi API createOrder
//            RestTemplate restTemplate = new RestTemplate();
//            String apiUrl = "http://localhost:8080/orders/create"; // Thay bằng endpoint thực tế
//
//            // Tạo requestBody từ tham số động
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("customerName", args[0]); // Tham số 1: Tên khách hàng
//            requestBody.put("phoneNumber", args[1]); // Tham số 2: Số điện thoại
//            requestBody.put("address", args[2]); // Tham số 3: Địa chỉ
//
//            // Thêm items động
//            Map<String, Object> item = new HashMap<>();
//            item.put("productId", args[3]); // Tham số 4: productId
//            item.put("quantity", Integer.parseInt(args[4])); // Tham số 5: quantity
//            requestBody.put("items", new Object[]{item});
//
//            // Gọi API và lấy response
//            Map<String, Object> response = restTemplate.postForObject(apiUrl, requestBody, Map.class);
//
//            if (response != null && response.containsKey("paymentUrl")) {
//                String paymentUrl = (String) response.get("paymentUrl");
//                generateQRCodeImage(paymentUrl, 300, 300, "src/main/resources/static/qrcode.png");
//            } else {
//                throw new RuntimeException("Error: Failed to get paymentUrl from response: " + response);
//            }
//        } catch (IllegalArgumentException e) {
//            System.err.println(e.getMessage());
//        } catch (HttpClientErrorException e) {
//            System.err.println("API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}