package com.javaweb.repository;

import com.javaweb.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR p.productName LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<ProductEntity> searchProductByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) ")
    Page<ProductEntity> searchProductByCategoryId(Long categoryId, Pageable pageable);
}
