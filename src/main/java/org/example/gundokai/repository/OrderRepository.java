package org.example.gundokai.repository;
import org.example.gundokai.entity.Order;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_Id(String userId);

    Page<Order> findByUser_Id(String userId, Pageable pageable);

    Page<Order> findByUser_IdAndStatus(String userId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
