package com.exam.library_management;

import com.exam.library_management.enums.Role;
import com.exam.library_management.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.CommandLineRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LibraryManagementApplicationUnitTest {

    @Test
    void testUserService_shouldRegisterDefaultUsers_whenMissing() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.existsByEmail("admin@library.com")).thenReturn(false);
        when(userService.existsByEmail("user@library.com")).thenReturn(false);

        CommandLineRunner runner = new LibraryManagementApplication().testUserService(userService);
        runner.run();

        verify(userService, times(1)).registerUser("LIB001", "admin@library.com", "admin123", Role.ADMIN);
        verify(userService, times(1)).registerUser("LIB002", "user@library.com", "user123", Role.USER);
    }

    @Test
    void testUserService_shouldSkipRegistration_whenUsersExist() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.existsByEmail("admin@library.com")).thenReturn(true);
        when(userService.existsByEmail("user@library.com")).thenReturn(true);

        CommandLineRunner runner = new LibraryManagementApplication().testUserService(userService);
        runner.run();

        verify(userService, never()).registerUser("LIB001", "admin@library.com", "admin123", Role.ADMIN);
        verify(userService, never()).registerUser("LIB002", "user@library.com", "user123", Role.USER);
    }
}
