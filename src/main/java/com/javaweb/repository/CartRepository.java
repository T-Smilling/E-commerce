package com.javaweb.repository;

import com.javaweb.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartEntity,Long> {
    @Query("SELECT c FROM CartEntity c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.productId = ?1")
    List<CartEntity> findCartsByProductId(Long productId);

    @Query("SELECT c FROM CartEntity c WHERE c.user.email = ?1 AND c.id = ?2")
    CartEntity findByEmailAndId(String email, Long cartId);
}
