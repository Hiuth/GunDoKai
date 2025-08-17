package org.example.gundokai.repository;

import org.example.gundokai.entity.PaymentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, String> {
    Optional<PaymentLog> findByOrderId(String orderId);

    // Lọc danh sách PaymentLog theo phương thức thanh toán
    Page<PaymentLog> findByMethod(String method, Pageable pageable);

    // Lọc danh sách PaymentLog theo phương thức thanh toán và trạng thái
    @Query("SELECT p FROM PaymentLog p WHERE p.method = :method AND p.status = :status")
    Page<PaymentLog> findByMethodAndStatus(String method, String status, Pageable pageable);

    // Lọc danh sách PaymentLog theo trạng thái (không phụ thuộc vào phương thức thanh toán)
    @Query("SELECT p FROM PaymentLog p WHERE p.status = :status")
    Page<PaymentLog> findByStatus(String status, Pageable pageable);
}