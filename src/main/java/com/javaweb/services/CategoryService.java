package com.javaweb.services;

import com.javaweb.model.dto.CategoryDTO;
import com.javaweb.model.response.CategoryResponse;
import com.javaweb.model.response.StatusResponse;
import jakarta.validation.Valid;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO category);

    CategoryResponse getAllCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);

    StatusResponse deleteCategory(Long categoryId);
}
