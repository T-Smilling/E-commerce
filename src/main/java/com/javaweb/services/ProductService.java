package com.javaweb.services;

import com.javaweb.model.dto.ProductDTO;
import com.javaweb.model.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, ProductDTO product);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOr);

    ProductResponse getProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOr);

    ProductDTO updateProduct(Long productId, ProductDTO productDTO);

    String deleteProduct(Long productId);

    ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOr);
}
