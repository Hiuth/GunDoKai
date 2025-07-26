package org.example.gundokai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String productName;
    long price;
    int stockQuantity;
    String status;
    String thumbnail;
    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToOne
    @JoinColumn(name = "subCategory_id", referencedColumnName = "id")
    SubCategory subcategory;

    LocalDateTime createdAt;
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
