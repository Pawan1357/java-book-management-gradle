package com.exam.library_management.dto;

import com.exam.library_management.enums.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookUpdateRequest {

    @Pattern(regexp = "^(?!\\s*$).+", message = "title must not be blank")
    @Size(max = 120, message = "title must not exceed 120 characters")
    @Schema(example = "Clean Code")
    private String title;

    @Pattern(regexp = "^(?!\\s*$).+", message = "author must not be blank")
    @Size(max = 80, message = "author must not exceed 80 characters")
    @Schema(example = "Robert C. Martin")
    private String author;

    @Schema(example = "AVAILABLE")
    private BookStatus status;

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

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    @AssertTrue(message = "At least one field (title, author, or status) must be provided")
    public boolean isAnyFieldProvided() {
        return title != null || author != null || status != null;
    }
}
