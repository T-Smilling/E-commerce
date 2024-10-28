package com.javaweb.controllers;

import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.response.InfoUserResponse;
import com.javaweb.model.response.UserResponse;
import com.javaweb.services.AuthenticationService;
import com.javaweb.services.UserService;
import com.javaweb.utils.MessageUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/${api.prefix}")
@SecurityRequirement(name = "E-Commerce")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/admin/users")
    public ResponseEntity<UserResponse> getUsers(
            @RequestParam(name = "pageNumber", defaultValue = MessageUtils.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = MessageUtils.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = MessageUtils.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = MessageUtils.SORT_DIR, required = false) String sortOrder) {

        UserResponse userResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(userResponse, HttpStatus.FOUND);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<InfoUserResponse> getUser(@PathVariable Long userId) {
        InfoUserResponse user = authenticationService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.FOUND);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long userId) {
        UserDTO updatedUser = authenticationService.updateUser(userId, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/confirm/{userId}")
    public ResponseEntity<String> confirmUser(@PathVariable Long userId, @RequestParam String secretCode, HttpServletResponse response) throws IOException {
        log.info("Confirm user userId={}, secretCode={}", userId, secretCode);
        try {
            userService.confirmUser(userId,secretCode);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("User confirmed!");
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Confirm failure");
        } finally {
//            response.sendRedirect("http://localhost:8085/api/v1/users/login");
            response.sendRedirect("https://github.com/T-Smilling");
        }
    }

    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String status = userService.deleteUser(userId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
