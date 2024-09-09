package com.javaweb.controllers;

import com.javaweb.model.dto.ProductDTO;
import com.javaweb.model.response.ProductResponse;
import com.javaweb.services.ProductService;
import com.javaweb.utils.MessageUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${api.prefix}")
@SecurityRequirement(name = "E-Commerce")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping(value = "/admin/category/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @PathVariable("categoryId") Long categoryId, @RequestBody ProductDTO productDTO){
        ProductDTO product = productService.addProduct(categoryId,productDTO);
        return new ResponseEntity<ProductDTO>(product, HttpStatus.CREATED);
    }

    @GetMapping(value = "/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = MessageUtils.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = MessageUtils.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = MessageUtils.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = MessageUtils.SORT_DIR, required = false) String sortOr) {
        ProductResponse productReponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOr);
        return new ResponseEntity<>(productReponse, HttpStatus.FOUND);
    }

    @GetMapping(value = "/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam(name = "pageNumber", defaultValue = MessageUtils.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = MessageUtils.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(name = "sortBy", defaultValue = MessageUtils.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder", defaultValue = MessageUtils.SORT_DIR, required = false) String sortOr) {
        ProductResponse productResponse = productService.getProductByKeyword(keyword,pageNumber,pageSize,sortBy,sortOr);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @GetMapping(value = "/products/keyword/{categoryId}")
    public ResponseEntity<ProductResponse> getProductsByCategoryId(@PathVariable Long categoryId,
                                                                @RequestParam(name = "pageNumber", defaultValue = MessageUtils.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = MessageUtils.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(name = "sortBy", defaultValue = MessageUtils.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder", defaultValue = MessageUtils.SORT_DIR, required = false) String sortOr) {
        ProductResponse productResponse = productService.getProductByCategoryId(categoryId,pageNumber,pageSize,sortBy,sortOr);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping(value = "/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable("productId") Long productId, @RequestBody ProductDTO productDTO){
        ProductDTO updateProduct = productService.updateProduct(productId,productDTO);
        return new ResponseEntity<ProductDTO>(updateProduct, HttpStatus.OK);
    }

    @DeleteMapping(value = "/admin/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long productId){
        String status = productService.deleteProduct(productId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}
