package org.example.gundokai.repository;

import org.example.gundokai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    boolean existsByProductName(String name);

    List<Product> findAllBySubcategory_Id(String subCategoryId);

    // Lấy 5 sản phẩm ngẫu nhiên
    @Query(value = "SELECT * FROM product ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Product> findRandomProducts();

}
