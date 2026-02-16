package com.exam.library_management.service;

import com.exam.library_management.dto.LoginRequest;
import com.exam.library_management.dto.LoginResponse;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@library.com");
        loginRequest.setPassword("password123");
    }

    // =====================================================
    // SUCCESS CASE
    // =====================================================
    @Test
    void login_success() {

        // Mock UserDetails
        UserDetails userDetails = User
                .withUsername("test@library.com")
                .password("encodedPassword")
                .roles("USER")
                .build();

        // Mock behavior
        when(userDetailsService.loadUserByUsername("test@library.com"))
                .thenReturn(userDetails);

        when(jwtUtil.generateToken(userDetails))
                .thenReturn("mocked-jwt-token");

        // Call service
        LoginResponse response = authService.login(loginRequest);

        // Assertions
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());

        // Verify interactions
        verify(authenticationManager, times(1))
                .authenticate(any());

        verify(userDetailsService, times(1))
                .loadUserByUsername("test@library.com");

        verify(jwtUtil, times(1))
                .generateToken(userDetails);
    }

    // =====================================================
    // FAILURE CASE - INVALID CREDENTIALS
    // =====================================================
    @Test
    void login_invalidCredentials_shouldThrowException() {

        // Mock authentication failure
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        // Ensure no further interactions
        verify(userDetailsService, never())
                .loadUserByUsername(any());

        verify(jwtUtil, never())
                .generateToken(any());
    }
}
