package org.example.gundokai.repository;

import org.example.gundokai.entity.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImgRepository extends JpaRepository<ProductImg,String> {
    List<ProductImg> findAllByProductId(String productId);
}
