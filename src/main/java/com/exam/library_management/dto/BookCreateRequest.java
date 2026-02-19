package com.exam.library_management.dto;

import jakarta.validation.constraints.NotBlank;

public class BookCreateRequest {

    @NotBlank
    private String bookCode;

    @NotBlank
    private String title;

    @NotBlank
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
