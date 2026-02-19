package com.exam.library_management.service;

import com.exam.library_management.dto.UserResponse;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.exception.DuplicateResourceException;
import com.exam.library_management.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private final String libraryId = "LIB100";
    private final String email = "test@library.com";
    private final String rawPassword = "password123";

    // =====================================================
    // SUCCESS CASE
    // =====================================================
    @Test
    void registerUser_success() {

        when(userRepository.findByEmailOrLibraryId(email, libraryId)).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setLibraryId(libraryId);
        savedUser.setEmail(email);
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.registerUser(
                libraryId,
                email,
                rawPassword,
                Role.USER
        );

        assertNotNull(response);
        assertEquals(libraryId, response.getLibraryId());
        assertEquals(email, response.getEmail());
        assertEquals(Role.USER, response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(passwordEncoder).encode(rawPassword);

        User persisted = userCaptor.getValue();
        assertEquals(libraryId, persisted.getLibraryId());
        assertEquals(email, persisted.getEmail());
        assertEquals("encodedPassword", persisted.getPassword());
        assertEquals(Role.USER, persisted.getRole());
        assertNotNull(persisted.getCreatedAt());
    }

    // =====================================================
    // EMAIL ALREADY EXISTS
    // =====================================================
    @Test
    void registerUser_emailAlreadyExists_shouldThrowException() {

        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setLibraryId("OTHER_LIB");
        when(userRepository.findByEmailOrLibraryId(email, libraryId))
                .thenReturn(java.util.Optional.of(existingUser));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.registerUser(
                        libraryId,
                        email,
                        rawPassword,
                        Role.USER
                )
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    // =====================================================
    // LIBRARY ID ALREADY EXISTS
    // =====================================================
    @Test
    void registerUser_libraryIdAlreadyExists_shouldThrowException() {

        User existingUser = new User();
        existingUser.setEmail("other@test.com");
        existingUser.setLibraryId(libraryId);
        when(userRepository.findByEmailOrLibraryId(email, libraryId))
                .thenReturn(java.util.Optional.of(existingUser));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.registerUser(
                        libraryId,
                        email,
                        rawPassword,
                        Role.USER
                )
        );

        assertEquals("Library ID already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    // =====================================================
    // DATA INTEGRITY EXCEPTION BRANCH
    // =====================================================
    @Test
    void registerUser_dataIntegrityViolation_shouldThrowDuplicateResourceException() {

        when(userRepository.findByEmailOrLibraryId(email, libraryId)).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("DB error"));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.registerUser(
                        libraryId,
                        email,
                        rawPassword,
                        Role.USER
                )
        );

        assertEquals("Email already exists", exception.getMessage());
    }

    // =====================================================
    // EXISTS BY EMAIL
    // =====================================================
    @Test
    void existsByEmail_shouldReturnTrue() {

        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void existsByEmail_shouldReturnFalse() {

        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userService.existsByEmail(email);

        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }
}
