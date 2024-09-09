package com.javaweb.controllers;

import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.response.UserResponse;
import com.javaweb.services.UserService;
import com.javaweb.utils.MessageUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${api.prefix}")
@SecurityRequirement(name = "E-Commerce")
public class UserController {
    @Autowired
    private UserService userService;

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
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);

        return new ResponseEntity<>(user, HttpStatus.FOUND);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long userId) {
        UserDTO updatedUser = userService.updateUser(userId, userDTO);

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String status = userService.deleteUser(userId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}