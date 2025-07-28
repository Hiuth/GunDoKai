package org.example.gundokai.repository;

import org.example.gundokai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    boolean existsByProductName(String name);

    List<Product> findAllBySubcategory_Id(String subCategoryId);
}
