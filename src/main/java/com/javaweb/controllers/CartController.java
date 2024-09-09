package com.javaweb.controllers;

import com.javaweb.model.dto.CartDTO;
import com.javaweb.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.prefix}")
@SecurityRequirement(name = "E-Commerce")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/carts/{cartId}/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long cartId,
                                                    @PathVariable Long productId,
                                                    @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(cartId, productId, quantity);

        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {

        List<CartDTO> cartDTOs = cartService.getAllCarts();

        return new ResponseEntity<>(cartDTOs, HttpStatus.FOUND);
    }

    @GetMapping("/users/{email}/carts/{cartId}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable String email, @PathVariable Long cartId) {
        CartDTO cartDTO = cartService.getCartById(email, cartId);

        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @PutMapping("/carts/{cartId}/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long cartId,
                                                     @PathVariable Long productId,
                                                     @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(cartId, productId, quantity);

        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
