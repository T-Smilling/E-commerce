package com.javaweb.controllers;

import com.javaweb.exception.UserNotFoundException;
import com.javaweb.model.dto.LoginCredentials;
import com.javaweb.model.dto.ResetPasswordDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.response.StatusResponse;
import com.javaweb.model.response.TokenResponse;
import com.javaweb.services.AuthenticationService;
import com.javaweb.services.UserService;
import com.javaweb.utils.JWTTokenUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "E-Commerce")
@Validated
@Slf4j
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

//    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> registerHandler(@Valid @RequestBody UserDTO user) throws Exception {
//        log.info("Received request to register user: {}", user);
//        UserDTO userDTO = userService.registerUser(user);
//
//        // Log thêm để kiểm tra userDTO
//        log.info("User registered: {}", userDTO);
//
//        // Tạo JWT token
//        String token;
//        try {
//            token = jwtTokenUtils.generateToken(userDTO.getEmail());
//            log.info("JWT token created: {}", token);
//        } catch (Exception e) {
//            log.error("Error creating JWT token: {}", e.getMessage());
//            throw e; // hoặc trả về một lỗi tùy ý
//        }
//
//        return new ResponseEntity<>(Collections.singletonMap("jwt-token", token), HttpStatus.CREATED);
//    }


    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginCredentials request){
        return new ResponseEntity<TokenResponse>(authenticationService.authenticate(request), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request){
        return new ResponseEntity<TokenResponse>(authenticationService.refresh(request), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
        return new ResponseEntity<String>(authenticationService.logout(request), HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StatusResponse> forgotPassword(@RequestBody String email){
        return new ResponseEntity<StatusResponse>(authenticationService.forgotPassword(email), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StatusResponse> resetPassword(@RequestBody String secretKey){
        return new ResponseEntity<StatusResponse>(authenticationService.resetPassword(secretKey), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<StatusResponse> changePassword(@RequestBody ResetPasswordDTO resetPasswordDTO){
        return new ResponseEntity<StatusResponse>(authenticationService.changePassword(resetPasswordDTO), HttpStatus.OK);
    }
}
