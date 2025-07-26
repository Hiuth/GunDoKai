package org.example.gundokai.repository;

import org.example.gundokai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    boolean existsByProductName(String name);
}
