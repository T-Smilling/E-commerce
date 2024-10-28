package com.javaweb.model.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCredentials {
    @NotBlank(message = "username must be not blank")
    private String name;

    @NotBlank(message = "username must be not blank")
    private String password;
}