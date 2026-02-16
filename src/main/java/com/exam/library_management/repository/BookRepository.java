package com.exam.library_management.repository;

import com.exam.library_management.entity.Book;
import com.exam.library_management.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookCode(String bookCode);

    List<Book> findByStatus(BookStatus status);
}
