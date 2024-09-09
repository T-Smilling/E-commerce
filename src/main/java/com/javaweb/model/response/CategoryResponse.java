package com.javaweb.model.response;

import com.javaweb.model.dto.CategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private List<CategoryDTO> content;
    private Integer number;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private boolean isLast;
}
