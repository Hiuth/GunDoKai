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
    @Query(value = "SELECT * FROM product WHERE stock_quantity > 0 ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Product> findRandomProducts();

    @Query("select c from Product c where c.productName like %:KeyWord%")
    List<Product> findProductByKeyWord(String KeyWord);
}
