package com.exam.library_management.integration;

import com.exam.library_management.entity.Book;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.repository.BookRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
public class BookRepositoryIntegrationTest {

    @Container
    public static final MySQLContainer<?> mysqlContainer =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private BookRepository bookRepository;

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Test
    void shouldSaveAndFindBookByBookCode() {

        Book book = new Book();
        book.setTitle("Repository Book");
        book.setAuthor("Repo Author");
        book.setBookCode("BOOK001");
        book.setStatus(BookStatus.AVAILABLE);

        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findByBookCode("BOOK001");

        assertTrue(foundBook.isPresent());
        assertEquals("Repository Book", foundBook.get().getTitle());
        assertEquals(BookStatus.AVAILABLE, foundBook.get().getStatus());
    }

    @Test
    void shouldFindBooksByStatus() {

        Book book1 = new Book();
        book1.setTitle("Available Book");
        book1.setAuthor("Author A");
        book1.setBookCode("BOOK002");
        book1.setStatus(BookStatus.AVAILABLE);

        Book book2 = new Book();
        book2.setTitle("Issued Book");
        book2.setAuthor("Author B");
        book2.setBookCode("BOOK003");
        book2.setStatus(BookStatus.BORROWED);

        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> availableBooks = bookRepository.findByStatus(BookStatus.AVAILABLE);

        assertEquals(1, availableBooks.size());
        assertEquals("BOOK002", availableBooks.get(0).getBookCode());
    }
}
