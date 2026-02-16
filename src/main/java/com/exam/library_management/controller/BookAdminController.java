package com.exam.library_management.controller;

import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.entity.Book;
import com.exam.library_management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BookAdminController {

    private final BookService bookService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Book>> addBook(@RequestBody Book book) {
        // return ResponseEntity.ok(bookService.addBook(book));
        Book saved = bookService.addBook(book);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book added successfully", saved)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {
        // return ResponseEntity.ok(bookService.updateBook(id, book));
        Book updated = bookService.updateBook(id, book);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book updated successfully", updated)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        // return ResponseEntity.noContent().build();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book deleted successfully", null)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        // return ResponseEntity.ok(bookService.getAllBooks());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Books fetched successfully",
                        bookService.getAllBooks())
        );
    }
}
