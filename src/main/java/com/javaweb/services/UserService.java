package com.javaweb.services;

import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.response.UserResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import java.io.UnsupportedEncodingException;

public interface UserService {
    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    UserDTO getUserById(Long userId);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    String deleteUser(Long userId);

    UserDTO registerUser(@Valid UserDTO user) throws MessagingException, UnsupportedEncodingException;

    void confirmUser(Long userId, String secretCode);
}
