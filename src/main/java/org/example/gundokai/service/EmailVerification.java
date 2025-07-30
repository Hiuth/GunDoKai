package org.example.gundokai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerification {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    // Data class to store code with timestamp
    private static class VerificationData {
        private final String code;
        private final long timestamp;

        public VerificationData(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    // Map lưu tạm code theo email với timestamp
    Map<String, VerificationData> codes = new ConcurrentHashMap<>();

    public void sendCode(String toEmail) {
        String email = toEmail.trim();              // ✂ trim email
        String code  = String.format("%06d", new Random().nextInt(1_000_000));
        codes.put(email, new VerificationData(code, System.currentTimeMillis()));

        log.debug("Generated verification code [{}] for email [{}]", code, email);
        log.debug("Current codes map size: {}", codes.size());

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(email);
        msg.setSubject("LetMeCook – Mã xác thực");
        msg.setText("Mã xác thực của bạn là: " + code + " (hết hạn trong 5 phút)");
        mailSender.send(msg);
    }

    public boolean verifyCode(String rawEmail, String rawCode) {
        String email = rawEmail.trim();             // ✂ trim email
        String code  = rawCode  == null ? "" : rawCode.trim();  // ✂ trim code

        VerificationData data = codes.get(email);
        log.debug("Verifying code [{}] for email [{}]", code, email);

        if (data == null) {
            log.debug("No verification code found for email [{}]", email);
            return false;
        }

        // Check if code has expired (5 minutes = 300,000 milliseconds)
        if (System.currentTimeMillis() - data.getTimestamp() > 300000) {
            log.debug("Verification code expired for email [{}]", email);
            codes.remove(email); // Clean up expired code
            return false;
        }

        if (data.getCode().equals(code)) {
            codes.remove(email); // Remove code after successful verification
            log.debug("Verification successful for email [{}]", email);
            return true;
        }

        log.debug("Invalid verification code for email [{}]", email);
        return false;
    }

    // Optional: Method to clean up expired codes periodically
    public void cleanupExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        codes.entrySet().removeIf(entry ->
                currentTime - entry.getValue().getTimestamp() > 300000
        );
        log.debug("Cleaned up expired codes. Remaining codes: {}", codes.size());
    }
}