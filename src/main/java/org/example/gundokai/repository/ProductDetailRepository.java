package org.example.gundokai.repository;

import org.example.gundokai.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, String> {
    ProductDetail findByProductId(String productId);
}
