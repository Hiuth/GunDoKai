package org.example.gundokai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
@Getter
@Setter
public class OrderDetail {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}