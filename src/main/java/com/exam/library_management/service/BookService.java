package com.exam.library_management.service;

import com.exam.library_management.entity.Book;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.repository.BookRepository;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /* ADMIN */
    public Book addBook(Book book) {
        // book.setId(null); // safety
        // book.setStatus(BookStatus.AVAILABLE);
        // return bookRepository.save(book);
        try {
            book.setStatus(BookStatus.AVAILABLE);
            return bookRepository.save(book);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Book with this code already exists");
        }
    }

    /* ADMIN */
    public Book updateBook(Long id, Book updated) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // book.setTitle(updated.getTitle());
        // book.setAuthor(updated.getAuthor());
        // book.setStatus(updated.getStatus());
        if (updated.getTitle() != null)
            book.setTitle(updated.getTitle());

        if (updated.getAuthor() != null)
            book.setAuthor(updated.getAuthor());

        if (updated.getStatus() != null)
            book.setStatus(updated.getStatus());

        return bookRepository.save(book);
    }

    /* ADMIN */
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found");
        }
        bookRepository.deleteById(id);
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
