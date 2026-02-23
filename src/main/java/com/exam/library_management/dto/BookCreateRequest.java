package com.exam.library_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookCreateRequest {

    @NotBlank(message = "bookCode is required")
    @Size(max = 30, message = "bookCode must not exceed 30 characters")
    @Schema(example = "BK-1001")
    private String bookCode;

    @NotBlank(message = "title is required")
    @Size(max = 120, message = "title must not exceed 120 characters")
    @Schema(example = "The Pragmatic Programmer")
    private String title;

    @NotBlank(message = "author is required")
    @Size(max = 80, message = "author must not exceed 80 characters")
    @Schema(example = "Andrew Hunt")
    private String author;

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
