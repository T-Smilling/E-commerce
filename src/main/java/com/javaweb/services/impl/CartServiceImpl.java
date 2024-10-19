package com.javaweb.services.impl;

import com.javaweb.entity.CartEntity;
import com.javaweb.entity.CartItemEntity;
import com.javaweb.entity.ProductEntity;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.CartDTO;
import com.javaweb.model.dto.ProductDTO;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.CartItemRepository;
import com.javaweb.services.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        CartEntity cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId)));

        ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        CartItemEntity cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        CartEntity cart = cartRepository.findById(cartId).
                orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId)));
        CartItemEntity cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", String.valueOf(productId));
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        ProductEntity product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return null;
    }

    @Override
    public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {
        CartEntity cartEntity = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId)));
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        CartItemEntity cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
        if (cartItem != null) {
            throw new APIException("Product " + productEntity.getProductName() + " already available in the cart!!!");
        }
        if (productEntity.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + productEntity.getProductName()
                    + " less than or equal to the quantity " + productEntity.getQuantity() + ".");
        }
        if (productEntity.getQuantity() == 0){
            throw new APIException(productEntity.getProductName() + " is not available");
        }
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCart(cartEntity);
        cartItemEntity.setProduct(productEntity);
        cartItemEntity.setQuantity(quantity);
        cartItemEntity.setDiscount(productEntity.getDiscount());
        cartItemEntity.setProductPrice(productEntity.getSpecialPrice());

        cartItemRepository.save(cartItemEntity);
        productEntity.setQuantity(productEntity.getQuantity() - quantity);

        cartEntity.setTotalPrice(cartEntity.getTotalPrice() + (productEntity.getSpecialPrice() * quantity));

        CartDTO cartDTO = modelMapper.map(cartEntity,CartDTO.class);

        List<ProductDTO> productDTOs = cartEntity.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<CartEntity> cartEntities = cartRepository.findAll();

        if (cartEntities.isEmpty()){
            throw new APIException("NO cart exists");
        }

        return cartEntities.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream().
                    map(product -> modelMapper.map(product.getProduct(),ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public CartDTO getCartById(String email, Long cartId) {
        CartEntity cartEntity = cartRepository.findByEmailAndId(email,cartId);

        if (cartEntity == null) {
            throw new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId));
        }

        CartDTO cartDTO = modelMapper.map(cartEntity, CartDTO.class);

        List<ProductDTO> products = cartEntity.getCartItems().stream()
                .map(product -> modelMapper.map(product.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Override
    public CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {
        CartEntity cartEntity = cartRepository.findById(cartId).
                orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId)));

        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        if (productEntity.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + productEntity.getProductName()
                    + " less than or equal to the quantity " + productEntity.getQuantity() + ".");
        }
        if (productEntity.getQuantity() == 0) {
            throw new APIException(productEntity.getProductName() + " is not available");
        }

        CartItemEntity cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null) {
            throw new APIException("Product " + productEntity.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cartEntity.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        productEntity.setQuantity(productEntity.getQuantity() + cartItem.getQuantity() - quantity);

        cartItem.setProductPrice(productEntity.getSpecialPrice());
        cartItem.setQuantity(quantity);
        cartItem.setDiscount(productEntity.getDiscount());

        cartItemRepository.save(cartItem);

        cartEntity.setTotalPrice(cartPrice + (cartItem.getProductPrice() * quantity));

        CartDTO cartDTO = modelMapper.map(cartEntity,CartDTO.class);

        List<ProductDTO> productDTOs = cartEntity.getCartItems().stream()
                .map(product -> modelMapper.map(product.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }
}
