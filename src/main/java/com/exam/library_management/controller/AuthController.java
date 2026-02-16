package com.exam.library_management.controller;

import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.dto.LoginRequest;
import com.exam.library_management.dto.LoginResponse;
import com.exam.library_management.dto.RegisterRequest;
import com.exam.library_management.dto.UserResponse;
import com.exam.library_management.enums.Role;
import com.exam.library_management.service.AuthService;
import com.exam.library_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // public AuthController(AuthService authService) {
    //     this.authService = authService;
    // }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse response = userService.registerUser(
            request.getLibraryId(),
            request.getEmail(),
            request.getPassword(),
            Role.USER
        );
        return ResponseEntity.ok(new ApiResponse<UserResponse>(true, "User registered successfully", response));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        // String token = authService.login(
        //         request.getUsername(),
        //         request.getPassword()
        // );

        // return ResponseEntity.ok(
        //         Map.of("token", token)
        // );
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
    }
}
