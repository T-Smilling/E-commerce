package com.javaweb.services;

import com.javaweb.exception.UserNotFoundException;
import com.javaweb.model.dto.LoginCredentials;
import com.javaweb.model.dto.ResetPasswordDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.response.InfoUserResponse;
import com.javaweb.model.response.StatusResponse;
import com.javaweb.model.response.TokenResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.io.UnsupportedEncodingException;

public interface AuthenticationService {
    InfoUserResponse getUserById(Long userId);

    TokenResponse authenticate(LoginCredentials request) throws UserNotFoundException;

    TokenResponse refresh(HttpServletRequest request);

    String logout(HttpServletRequest request);

    StatusResponse forgotPassword(String email);

    StatusResponse resetPassword(String secretKey);

    StatusResponse changePassword(ResetPasswordDTO resetPasswordDTO);

    UserDTO registerUser(@Valid UserDTO user) throws MessagingException, UnsupportedEncodingException;

    UserDTO updateUser(Long userId, UserDTO userDTO);
}
