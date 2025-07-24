package org.example.gundokai.repository;

import org.example.gundokai.entity.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory,String> {
    boolean existsByCategoryName(String name);
}
