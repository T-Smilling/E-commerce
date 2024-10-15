package com.javaweb.model.dto;

import com.javaweb.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private Set<RoleEntity> roles = new HashSet<>();
    private AddressDTO address;
    private CartDTO cart;
}