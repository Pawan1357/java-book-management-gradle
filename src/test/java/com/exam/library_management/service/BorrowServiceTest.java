package com.exam.library_management.service;

import com.exam.library_management.config.LibraryProperties;
import com.exam.library_management.entity.Book;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.exception.ResourceNotFoundException;
import com.exam.library_management.repository.BookRepository;
import com.exam.library_management.repository.BorrowRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryProperties libraryProperties;

    @InjectMocks
    private BorrowService borrowService;

    private User user;
    private Book book;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        book = new Book();
        book.setId(1L);
        book.setStatus(BookStatus.AVAILABLE);
    }

    /* =====================================
       BORROW BOOK
       ===================================== */

    @Test
    void borrowBook_shouldThrowIfUserAlreadyHasActiveBorrow() {

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.of(new BorrowRecord()));

        assertThrows(BadRequestException.class,
                () -> borrowService.borrowBook(user, 1L));
    }

    @Test
    void borrowBook_shouldThrowIfBookNotFound() {

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.empty());

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> borrowService.borrowBook(user, 1L));
    }

    @Test
    void borrowBook_shouldThrowIfBookNotAvailable() {

        book.setStatus(BookStatus.BORROWED);

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.empty());

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThrows(BadRequestException.class,
                () -> borrowService.borrowBook(user, 1L));
    }

    @Test
    void borrowBook_shouldBorrowSuccessfully() {

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.empty());

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(libraryProperties.getBorrowDurationDays())
                .thenReturn(14);

        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowRecord result = borrowService.borrowBook(user, 1L);

        assertEquals(BookStatus.BORROWED, book.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());

        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    /* =====================================
       RETURN BOOK
       ===================================== */

    @Test
    void returnBook_shouldThrowIfNoActiveBorrow() {

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> borrowService.returnBook(user));
    }

    @Test
    void returnBook_shouldReturnWithoutLateFee_WhenOnTime() {

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setDueDate(LocalDate.now().plusDays(5)); // not late

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.of(record));

        when(borrowRecordRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowRecord result = borrowService.returnBook(user);

        assertEquals(BigDecimal.ZERO, result.getLateFee());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());

        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(record);
    }

    @Test
    void returnBook_shouldCalculateLateFee_WhenLate() {

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setDueDate(LocalDate.now().minusDays(3)); // 3 days late

        when(borrowRecordRepository.findByUserAndReturnDateIsNull(user))
                .thenReturn(Optional.of(record));

        when(libraryProperties.getLateFeePerDay())
                .thenReturn(10);

        when(borrowRecordRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowRecord result = borrowService.returnBook(user);

        BigDecimal expected = BigDecimal.valueOf(3)
                .multiply(BigDecimal.valueOf(10));

        assertEquals(expected, result.getLateFee());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());

        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(record);
    }
}
