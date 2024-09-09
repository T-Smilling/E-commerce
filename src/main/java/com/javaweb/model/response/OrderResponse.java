package com.javaweb.model.response;

import com.javaweb.model.dto.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private List<OrderDTO> content;
    private Integer number;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private boolean isLast;
}
