package com.exam.library_management.dto;

import com.exam.library_management.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyReport {

    private YearMonth month;
    private List<BorrowRecord> booksBorrowed;
    private List<BorrowRecord> booksReturned;
    private List<BorrowRecord> overdueBooks;
    private List<UserActivitySummary> userActivity;
}
