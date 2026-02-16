package com.exam.library_management.controller;

import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.entity.Book;
import com.exam.library_management.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    // -------------------------------------------------
    // ADD BOOK
    // -------------------------------------------------
    @Test
    @DisplayName("Add book - success")
    @WithMockUser(roles = "ADMIN")
    void addBook_success() throws Exception {

        Book book = new Book();
        book.setTitle("Spring Boot");

        when(bookService.addBook(any(Book.class)))
                .thenReturn(book);

        mockMvc.perform(post("/api/admin/books/add")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book added successfully"))
                .andExpect(jsonPath("$.data.title").value("Spring Boot"));

        verify(bookService, times(1)).addBook(any(Book.class));
    }

    // -------------------------------------------------
    // UPDATE BOOK
    // -------------------------------------------------
    @Test
    @DisplayName("Update book - success")
    @WithMockUser(roles = "ADMIN")
    void updateBook_success() throws Exception {

        Book updated = new Book();
        updated.setTitle("Updated");

        when(bookService.updateBook(eq(1L), any(Book.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/admin/books/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated"));

        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    // -------------------------------------------------
    // DELETE BOOK
    // -------------------------------------------------
    @Test
    @DisplayName("Delete book - success")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_success() throws Exception {

        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/admin/books/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(bookService, times(1)).deleteBook(1L);
    }

    // -------------------------------------------------
    // GET ALL BOOKS
    // -------------------------------------------------
    @Test
    @DisplayName("Get all books - success")
    @WithMockUser(roles = "ADMIN")
    void getAllBooks_success() throws Exception {

        Book book = new Book();
        book.setTitle("Spring");

        when(bookService.getAllBooks())
                .thenReturn(List.of(book));

        mockMvc.perform(get("/api/admin/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Books fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Spring"));

        verify(bookService, times(1)).getAllBooks();
    }
}
