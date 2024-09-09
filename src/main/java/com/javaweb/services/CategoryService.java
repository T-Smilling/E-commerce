package com.javaweb.services;

import com.javaweb.model.dto.CategoryDTO;
import com.javaweb.model.response.CategoryResponse;
import jakarta.validation.Valid;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO category);

    CategoryResponse getAllCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);

    String deleteCategory(Long categoryId);
}
