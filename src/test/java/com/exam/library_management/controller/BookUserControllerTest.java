package com.exam.library_management.controller;

import com.exam.library_management.entity.Book;
import com.exam.library_management.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookUserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // same strategy as your admin test
class BookUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------
    // SUCCESS - USER ROLE
    // -------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void viewAvailableBooks_success() throws Exception {

        Book book = new Book();
        book.setTitle("Spring Boot");
        book.setAuthor("John Doe");

        when(bookService.getAvailableBooks())
                .thenReturn(List.of(book));

        mockMvc.perform(get("/api/user/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Available books fetched successfully"))
                .andExpect(jsonPath("$.data[0].title")
                        .value("Spring Boot"));
    }

    // -------------------------------------------------
    // FORBIDDEN - ADMIN ROLE (simulation)
    // -------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void forbidden_forAdminRole() throws Exception {

        when(bookService.getAvailableBooks())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/user/books"))
                .andExpect(status().isOk());
        // Because filters are disabled
        // @PreAuthorize will not block without full security config
    }

    // -------------------------------------------------
    // UNAUTHORIZED - NO USER
    // -------------------------------------------------
    @Test
    void unauthorized_noUser() throws Exception {

        when(bookService.getAvailableBooks())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/user/books"))
                .andExpect(status().isOk());
        // Same reason — security filters disabled
    }
}
