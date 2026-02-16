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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final LibraryProperties libraryProperties;

    private static final int BORROW_DAYS = 14;
    private static final BigDecimal LATE_FEE_PER_DAY = BigDecimal.valueOf(10);

    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);

    /* =========================
       BORROW BOOK
       ========================= */
    public BorrowRecord borrowBook(User user, Long bookId) {

        // Rule 1: Only one active borrow per user
        borrowRecordRepository.findByUserAndReturnDateIsNull(user)
                .ifPresent(br -> {
                    throw new BadRequestException(
                            "You already have a borrowed book. Please return it first."
                    );
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        ));

        // Rule 2: Book must be available
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BadRequestException(
                    "This book is currently not available for borrowing"
            );
        }

        // Update book
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);

        // Create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(libraryProperties.getBorrowDurationDays()));

        return borrowRecordRepository.save(record);
    }

    /* =========================
       RETURN BOOK
       ========================= */
    public BorrowRecord returnBook(User user) {

        logger.info("BorrowService.returnBook called for userId={}", user.getId());

        BorrowRecord record = borrowRecordRepository
                .findByUserAndReturnDateIsNull(user)
                .orElseThrow(() ->
                        new BadRequestException(
                                "You do not have any borrowed book to return"
                        ));

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);

        BigDecimal lateFee = BigDecimal.ZERO;
        // Late fee calculation
        if (returnDate.isAfter(record.getDueDate())) {
            long daysLate =
                    ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
        //     record.setLateFee(
        //             LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(daysLate))
        //     );
        //     lateFee = LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(daysLate));
                lateFee = BigDecimal
                            .valueOf(daysLate)
                            .multiply(
                                    BigDecimal.valueOf(
                                            libraryProperties.getLateFeePerDay()
                                    )
                            );
        }
        record.setLateFee(lateFee);
        //  else {
        //     record.setLateFee(BigDecimal.ZERO);
        // }

        // Update book
        Book book = record.getBook();
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        logger.info("Return record saved: recordId={}, lateFee={}", savedRecord.getId(), savedRecord.getLateFee());

        return savedRecord;
    }
}
