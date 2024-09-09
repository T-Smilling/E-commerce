package com.javaweb.model.response;

import com.javaweb.model.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private List<ProductDTO> content;
    private Integer number;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private boolean isLast;
}
