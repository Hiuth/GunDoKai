package org.example.gundokai.repository;
import org.example.gundokai.entity.Order;
import org.example.gundokai.enums.OrderStatus;
import org.example.gundokai.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_Id(String userId);

    Page<Order> findByUser_Id(String userId, Pageable pageable);

    Page<Order> findByUser_IdAndStatus(String userId, OrderStatus status, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    @Query("SELECT o FROM Order o JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") String id);
    // ✅ Thêm method sort theo orderDate
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    Page<Order> findByUser_IdOrderByOrderDateDesc(@Param("userId") String userId, Pageable pageable);

    // ✅ Method lấy orders có payment thành công
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
            "AND (o.paymentStatus = 'CONFIRMED' OR o.paymentStatus = 'PAID' OR " +
            "(o.paymentMethod = 'COD' AND o.paymentStatus = 'PENDING')) " +
            "ORDER BY o.orderDate DESC")
    Page<Order> findSuccessfulOrdersByUserId(@Param("userId") String userId, Pageable pageable);

  ;
}

