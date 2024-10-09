package com.javaweb.repository;

import com.javaweb.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItemEntity,Long> {
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.product.productId = ?1 AND ci.cart.id = ?2")
    CartItemEntity findCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Modifying
    @Query("DELETE FROM CartItemEntity ci WHERE ci.cart.id = ?1 AND ci.product.productId = ?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);
}
