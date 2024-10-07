package com.javaweb.services.impl;

import com.javaweb.entity.CartEntity;
import com.javaweb.entity.CategoryEntity;
import com.javaweb.entity.ProductEntity;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.CartDTO;
import com.javaweb.model.dto.ProductDTO;
import com.javaweb.model.response.ProductResponse;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.ProductRepository;
import com.javaweb.services.CartService;
import com.javaweb.services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO product) {
        CategoryEntity category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", String.valueOf(categoryId)));
        boolean isProductNotPresent = true;
        List<ProductEntity> products = category.getProducts();
        for (ProductEntity productEntity : products) {
            if (productEntity.getProductName().equals(product.getProductName()) && productEntity.getDescription().equals(product.getDescription())) {
                isProductNotPresent = false;
                break;
            }
        }
        if (isProductNotPresent) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setCategory(category);
            double specialPrice = productEntity.getPrice() - ((productEntity.getDiscount() * 0.01) * productEntity.getPrice());
            productEntity.setSpecialPrice(specialPrice);
            productRepository.save(productEntity);
            return modelMapper.map(productEntity, ProductDTO.class);
        } else {
            throw new APIException("Product already exists !!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOr) {
        Sort sort = sortOr.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<ProductEntity> pageProducts = productRepository.findAll(pageable);
        List<ProductEntity> productList = pageProducts.getContent();
        List<ProductDTO> productDTOList = productList.stream().map(product -> modelMapper.map(product,ProductDTO.class)).collect(Collectors.toList());

        ProductResponse productReponse = modelMapper.map(pageProducts, ProductResponse.class);
        productReponse.setContent(productDTOList);

        return productReponse;
    }

    @Override
    public ProductResponse getProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOr) {
        Sort sort = sortOr.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<ProductEntity> pageProducts = productRepository.searchProductByKeyword(keyword,pageable);
        List<ProductEntity> productList = pageProducts.getContent();

        if (productList.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        List<ProductDTO> productDTOList = productList.stream().map(product -> modelMapper.map(product,ProductDTO.class)).collect(Collectors.toList());
        ProductResponse productResponse = modelMapper.map(pageProducts, ProductResponse.class);
        productResponse.setContent(productDTOList);

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        ProductEntity productEntity = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        if (productEntity == null) {
            throw new APIException("Product not found with productId: " + productId);
        }
        ProductEntity saveProduct = modelMapper.map(productDTO, ProductEntity.class);

        double specialPrice = productDTO.getPrice() - ((productDTO.getDiscount() * 0.01) * productDTO.getPrice());

        saveProduct.setCategory(productEntity.getCategory());
        saveProduct.setProductId(productId);
        saveProduct.setSpecialPrice(specialPrice);

        productRepository.save(saveProduct);

        List<CartEntity> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        for (CartDTO cart : cartDTOs) {
            cartService.updateProductInCarts(cart.getCartId(), productId);
        }
        return modelMapper.map(productEntity, ProductDTO.class);
    }

    @Override
    public String deleteProduct(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));
        List<CartEntity> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getId(), productId));
        productRepository.deleteById(productId);
        return "Product with productId: " + productId + " deleted successfully !!!";
    }

    @Override
    public ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOr) {
        Sort sort = sortOr.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<ProductEntity> pageProducts = productRepository.searchProductByCategoryId(categoryId,pageable);
        List<ProductEntity> productList = pageProducts.getContent();

        if (productList.isEmpty()) {
            throw new APIException("Products not found with categoryId: " + categoryId);
        }

        List<ProductDTO> productDTOList = productList.stream().map(product -> modelMapper.map(product,ProductDTO.class)).collect(Collectors.toList());
        ProductResponse productResponse = new ProductResponse();

        ProductResponse productReponse = modelMapper.map(pageProducts, ProductResponse.class);
        productReponse.setContent(productDTOList);

        return productResponse;
    }

}
