package com.javaweb.services.impl;

import com.javaweb.entity.CategoryEntity;
import com.javaweb.entity.ProductEntity;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.CategoryDTO;
import com.javaweb.model.response.CategoryResponse;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.ProductRepository;
import com.javaweb.services.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public CategoryDTO createCategory(CategoryDTO category) {
        CategoryEntity categoryEntity = categoryRepository.findByCategoryName(category.getCategoryName());

        if (categoryEntity != null) {
            throw new APIException("Category with the name '" + category.getCategoryName() + "' already exists !!!");
        }
        categoryEntity.setStatus("1");
        categoryRepository.save(categoryEntity);
        return modelMapper.map(categoryEntity, CategoryDTO.class);
    }

    @Override
    public CategoryResponse getAllCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<CategoryEntity> pageCategory = categoryRepository.findAll(pageable);
        List<CategoryEntity> categoryList = pageCategory.getContent();

        if (categoryList.isEmpty()) {
            throw new APIException("No category is created till now");
        }

        List<CategoryDTO> categoryDTOList = categoryList.stream().map(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            if (category.getStatus().equals("1")){
                categoryDTO = modelMapper.map(category, CategoryDTO.class);
            }
            return categoryDTO;
        }).collect(Collectors.toList());

        CategoryResponse categoryResponse = modelMapper.map(pageCategory, CategoryResponse.class);

        categoryResponse.setContent(categoryDTOList);

        return categoryResponse;
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", String.valueOf(categoryId)));

        categoryEntity.setCategoryName(categoryDTO.getCategoryName());
        categoryEntity.setId(categoryId);

        categoryRepository.save(categoryEntity);

        return modelMapper.map(categoryEntity, CategoryDTO.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", String.valueOf(categoryId)));

        List<ProductEntity> productEntities = categoryEntity.getProducts();
        productRepository.deleteAll(productEntities);

        categoryEntity.setStatus("0");
        return "Category with categoryId: " + categoryId + " deleted successfully !!!";
    }
}
