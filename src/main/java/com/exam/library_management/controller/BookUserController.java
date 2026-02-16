package com.exam.library_management.controller;

import com.exam.library_management.entity.Book;
import com.exam.library_management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.exam.library_management.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/user/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class BookUserController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> viewAvailableBooks() {
        // return ResponseEntity.ok(bookService.getAvailableBooks());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Available books fetched successfully",
                        bookService.getAvailableBooks()
                )
        );
    }
}
