package com.exam.library_management.service;

import com.exam.library_management.dto.UserActivitySummary;
import com.exam.library_management.entity.Book;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.entity.User;
import com.exam.library_management.repository.BorrowRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceTest {

    @Mock
    private BorrowRecordRepository borrowRepo;

    @InjectMocks
    private MonthlyReportService monthlyReportService;

    @Test
    void generateMonthlyReport_shouldCallAllRepositoryMethods() {
    
        // Create nested objects to avoid NullPointerException
        Book book = new Book();
        book.setTitle("Spring Boot");
    
        User user = new User();
        user.setEmail("user@test.com");
    
        BorrowRecord record = new BorrowRecord();
        record.setBook(book);
        record.setUser(user);
        record.setLateFee(BigDecimal.TEN);
    
        UserActivitySummary summary =
                new UserActivitySummary(
                        1L,
                        "user@test.com",
                        2L,
                        1L
                );
            
        when(borrowRepo.findByBorrowDateBetween(any(), any()))
                .thenReturn(List.of(record));
            
        when(borrowRepo.findByReturnDateBetween(any(), any()))
                .thenReturn(List.of(record));
            
        when(borrowRepo.findOverdueBooks(any()))
                .thenReturn(List.of(record));
            
        when(borrowRepo.getUserActivitySummary(any(), any()))
                .thenReturn(List.of(summary));
            
        // Act
        monthlyReportService.generateMonthlyReport();
            
        // Verify
        verify(borrowRepo).findByBorrowDateBetween(any(), any());
        verify(borrowRepo).findByReturnDateBetween(any(), any());
        verify(borrowRepo).findOverdueBooks(any());
        verify(borrowRepo).getUserActivitySummary(any(), any());
    }


    @Test
    void generateMonthlyReport_shouldHandleEmptyData() {

        when(borrowRepo.findByBorrowDateBetween(any(), any()))
                .thenReturn(List.of());

        when(borrowRepo.findByReturnDateBetween(any(), any()))
                .thenReturn(List.of());

        when(borrowRepo.findOverdueBooks(any()))
                .thenReturn(List.of());

        when(borrowRepo.getUserActivitySummary(any(), any()))
                .thenReturn(List.of());

        monthlyReportService.generateMonthlyReport();

        verify(borrowRepo).findByBorrowDateBetween(any(), any());
        verify(borrowRepo).findByReturnDateBetween(any(), any());
        verify(borrowRepo).findOverdueBooks(any());
        verify(borrowRepo).getUserActivitySummary(any(), any());
    }

    @Test
    void generateMonthlyReport_shouldPassPreviousMonthDateRange() {

        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate expectedStart = previousMonth.atDay(1);
        LocalDate expectedEnd = previousMonth.atEndOfMonth();

        when(borrowRepo.findByBorrowDateBetween(any(), any()))
                .thenReturn(List.of());

        when(borrowRepo.findByReturnDateBetween(any(), any()))
                .thenReturn(List.of());

        when(borrowRepo.findOverdueBooks(any()))
                .thenReturn(List.of());

        when(borrowRepo.getUserActivitySummary(any(), any()))
                .thenReturn(List.of());

        monthlyReportService.generateMonthlyReport();

        verify(borrowRepo).findByBorrowDateBetween(expectedStart, expectedEnd);
        verify(borrowRepo).findByReturnDateBetween(expectedStart, expectedEnd);
        verify(borrowRepo).getUserActivitySummary(expectedStart, expectedEnd);
        verify(borrowRepo).findOverdueBooks(expectedEnd);
    }
}
