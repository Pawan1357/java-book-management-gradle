package com.exam.library_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "libraryId is required")
    @Size(min = 3, max = 30, message = "libraryId must be between 3 and 30 characters")
    @Schema(example = "LIB1001")
    private String libraryId;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 64, message = "password must be between 8 and 64 characters")
    @Schema(example = "Secret@123")
    private String password;

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
