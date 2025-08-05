package org.example.gundokai.repository;

import org.example.gundokai.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, String> {
    Optional<PaymentLog> findByOrderId(String orderId);
}