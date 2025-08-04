package org.example.gundokai.repository;

import org.example.gundokai.entity.Order;
import org.example.gundokai.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    List<OrderDetail> findByOrder(Order order);
}