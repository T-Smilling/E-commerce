package com.javaweb.controllers;

import com.javaweb.model.dto.LoginCredentials;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.services.UserService;
import com.javaweb.utils.JWTTokenUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/${api.prefix}/users")
@SecurityRequirement(name = "E-Commerce")
@Slf4j
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JWTTokenUtils jwtTokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerHandler(@Valid @RequestBody UserDTO user) throws Exception {
        log.info("Received request to register user: {}", user);
        UserDTO userDTO = userService.registerUser(user);

        // Log thêm để kiểm tra userDTO
        log.info("User registered: {}", userDTO);

        // Tạo JWT token
        String token;
        try {
            token = jwtTokenUtils.generateToken(userDTO.getEmail());
            log.info("JWT token created: {}", token);
        } catch (Exception e) {
            log.error("Error creating JWT token: {}", e.getMessage());
            throw e; // hoặc trả về một lỗi tùy ý
        }

        return new ResponseEntity<>(Collections.singletonMap("jwt-token", token), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public Map<String, Object> loginHandler(@Valid @RequestBody LoginCredentials credentials) throws Exception {

        UsernamePasswordAuthenticationToken authCredentials = new UsernamePasswordAuthenticationToken(
                credentials.getEmail(), credentials.getPassword());

        authenticationManager.authenticate(authCredentials);

        String token = jwtTokenUtils.generateToken(credentials.getEmail());

        return Collections.singletonMap("jwt-token", token);
    }
}
