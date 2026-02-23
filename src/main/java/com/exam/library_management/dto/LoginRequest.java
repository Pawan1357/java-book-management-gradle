package com.exam.library_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 100, message = "username must not exceed 100 characters")
    @Schema(example = "user@example.com")
    private String username; // email or libraryId

    @NotBlank(message = "password is required")
    @Size(max = 64, message = "password must not exceed 64 characters")
    @Schema(example = "Secret@123")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
