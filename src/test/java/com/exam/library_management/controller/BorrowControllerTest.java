package com.exam.library_management.controller;

import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.UserRepository;
import com.exam.library_management.service.BorrowService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // =====================================================
    // BORROW BOOK - SUCCESS
    // =====================================================
    @Test
    void borrowBook_success() throws Exception {

        String email = "user@library.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);

        BorrowRecord record = new BorrowRecord();
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(7));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(borrowService.borrowBook(eq(user), eq(1L)))
                .thenReturn(record);

        mockMvc.perform(post("/api/user/borrow/book/1")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Book borrowed successfully"))
                .andExpect(jsonPath("$.data.borrowDate").exists())
                .andExpect(jsonPath("$.data.dueDate").exists());
    }

    // =====================================================
    // BORROW BOOK - ROLE NOT USER
    // =====================================================
    @Test
    void borrowBook_adminShouldFail() throws Exception {

        String email = "admin@library.com";

        User admin = new User();
        admin.setEmail(email);
        admin.setRole(Role.ADMIN);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/user/borrow/book/1")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // RETURN BOOK - SUCCESS
    // =====================================================
    @Test
    void returnBook_success() throws Exception {

        String email = "user@library.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setLateFee(BigDecimal.TEN);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(borrowService.returnBook(user))
                .thenReturn(record);

        mockMvc.perform(post("/api/user/borrow/return")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Book returned successfully"))
                .andExpect(jsonPath("$.data.lateFee").value(10));
    }

    // =====================================================
    // RETURN BOOK - NO AUTHENTICATION
    // =====================================================
    @Test
    void returnBook_noAuthentication_shouldFail() throws Exception {

        mockMvc.perform(post("/api/user/borrow/return"))
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // RETURN BOOK - USER NOT FOUND
    // =====================================================
    @Test
    void returnBook_userNotFound_shouldFail() throws Exception {

        String email = "user@library.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/user/borrow/return")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // RETURN BOOK - NULL LATE FEE (SHOULD DEFAULT TO ZERO)
    // =====================================================
    @Test
    void returnBook_nullLateFee_shouldReturnZero() throws Exception {
    
        String email = "user@library.com";
    
        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);
    
        BorrowRecord record = new BorrowRecord();
        record.setId(2L);
        record.setLateFee(null);   // 🔥 Important branch
    
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
    
        when(borrowService.returnBook(user))
                .thenReturn(record);
    
        mockMvc.perform(post("/api/user/borrow/return")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lateFee").value(0));
    }

}
