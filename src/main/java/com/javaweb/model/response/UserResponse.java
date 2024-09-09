package com.javaweb.model.response;

import com.javaweb.model.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private List<UserDTO> content;
    private Integer number;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private boolean isLast;
}
