package com.exam.library_management.controller;

import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.dto.BookCreateRequest;
import com.exam.library_management.dto.BookUpdateRequest;
import com.exam.library_management.entity.Book;
import com.exam.library_management.service.BookService;
import jakarta.validation.Valid;
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

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Book>> addBook(@Valid @RequestBody BookCreateRequest request) {
        Book saved = bookService.addBook(toBook(request));
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book added successfully", saved)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        Book updated = bookService.updateBook(id, toBook(request));
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book updated successfully", updated)
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Book deleted successfully", null)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Books fetched successfully",
                        bookService.getAllBooks())
        );
    }

    private Book toBook(BookCreateRequest request) {
        Book book = new Book();
        book.setBookCode(request.getBookCode());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        return book;
    }

    private Book toBook(BookUpdateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setStatus(request.getStatus());
        return book;
    }
}
