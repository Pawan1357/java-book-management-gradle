package com.exam.library_management.service;

import com.exam.library_management.entity.Book;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.repository.BookRepository;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /* ADMIN */
    @Transactional
    public Book addBook(Book book) {
        try {
            book.setStatus(BookStatus.AVAILABLE);
            return bookRepository.save(book);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Book with this code already exists");
        }
    }

    /* ADMIN */
    @Transactional
    public Book updateBook(Long id, Book updated) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (updated.getTitle() == null
                && updated.getAuthor() == null
                && updated.getStatus() == null) {
            return book;
        }

        if (updated.getTitle() != null) {
            book.setTitle(updated.getTitle());
        }

        if (updated.getAuthor() != null) {
            book.setAuthor(updated.getAuthor());
        }

        if (updated.getStatus() != null) {
            book.setStatus(updated.getStatus());
        }
        return book;
    }

    /* ADMIN */
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        try {
            bookRepository.delete(book);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException(
                    "Cannot delete book because it is linked to borrow records"
            );
        }
    }

    /* ADMIN */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /* USER */
    public List<Book> getAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE);
    }
}
