package org.example.gundokai.repository;

import org.example.gundokai.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory,String> {
    boolean existsBySubCategoryName(String name);

    List<SubCategory> findAllByMainCategoryId(String name);
}
