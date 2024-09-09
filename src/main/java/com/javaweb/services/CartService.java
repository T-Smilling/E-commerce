package com.javaweb.services;

import com.javaweb.model.dto.CartDTO;

import java.util.List;

public interface CartService {
    void updateProductInCarts(Long cartId, Long productId);

    String deleteProductFromCart(Long CartId, Long productId);

    CartDTO addProductToCart(Long cartId, Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCartById(String email, Long cartId);

    CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity);
}
