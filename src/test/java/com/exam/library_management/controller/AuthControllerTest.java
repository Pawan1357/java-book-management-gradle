package com.exam.library_management.controller;

import com.exam.library_management.service.AuthService;
import com.exam.library_management.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exam.library_management.dto.RegisterRequest;
import com.exam.library_management.dto.UserResponse;
import com.exam.library_management.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.exam.library_management.dto.LoginRequest;
import com.exam.library_management.dto.LoginResponse;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    void registerUser_success() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setLibraryId("LIB123");
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        
        // UserResponse mockResponse = new UserResponse();
        // mockResponse.setEmail("test@gmail.com");
        // mockResponse.setLibraryId("LIB123");
        UserResponse mockResponse =
        new UserResponse("LIB123", "test@gmail.com", Role.USER);
        
        Mockito.when(userService.registerUser(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.eq(Role.USER)
        )).thenReturn(mockResponse);
    
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"));
    } 
    
    @Test
    void login_success() throws Exception {
    
        LoginRequest request = new LoginRequest();
        request.setUsername("test@gmail.com");
        request.setPassword("password123");
    
        LoginResponse mockResponse = new LoginResponse("jwt-token");
    
        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(mockResponse);
    
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }
}
