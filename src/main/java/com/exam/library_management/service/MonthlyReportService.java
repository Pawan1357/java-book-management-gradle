package com.exam.library_management.service;

import com.exam.library_management.dto.MonthlyReport;
import com.exam.library_management.dto.UserActivitySummary;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.repository.BorrowRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@Profile("!test")
public class MonthlyReportService {

    private final BorrowRecordRepository borrowRepo;
    private final String reportCron;

    public MonthlyReportService(
            BorrowRecordRepository borrowRepo,
            @Value("${library.report.cron:0 59 23 L * ?}") String reportCron
    ) {
        this.borrowRepo = borrowRepo;
        this.reportCron = reportCron;
    }

    @Scheduled(cron = "${library.report.cron:0 59 23 L * ?}")
    public void generateMonthlyReport() {

        // LocalDate start = LocalDate.now().withDayOfMonth(1);
        // LocalDate end = LocalDate.now();

        // List<BorrowRecord> borrowed = borrowRepo
        //     .findByBorrowDateBetween(start, end);

        // List<BorrowRecord> returned = borrowRepo
        //     .findByReturnDateBetween(start, end);

        // List<BorrowRecord> overdue = borrowRepo
        //     .findOverdueBooks(end);

        // System.out.println("📊 Monthly Report");
        // System.out.println("Borrowed: " + borrowed.size());
        // System.out.println("Returned: " + returned.size());
        // System.out.println("Overdue: " + overdue.size());
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate start = previousMonth.atDay(1);
        LocalDate end = previousMonth.atEndOfMonth();

        MonthlyReport report = buildMonthlyReport(start, end, previousMonth);

        log.info("Running monthly report scheduler with cron: {}", reportCron);
        logReport(report);
    }

    private MonthlyReport buildMonthlyReport(
            LocalDate start,
            LocalDate end,
            YearMonth month
    ) {
        List<BorrowRecord> borrowed =
                borrowRepo.findByBorrowDateBetween(start, end);

        List<BorrowRecord> returned =
                borrowRepo.findByReturnDateBetween(start, end);

        List<BorrowRecord> overdue =
                borrowRepo.findOverdueBooks(end);

        List<UserActivitySummary> userActivity =
                borrowRepo.getUserActivitySummary(start, end);

        return new MonthlyReport(
                month,
                borrowed,
                returned,
                overdue,
                userActivity
        );
    }

    private void logReport(MonthlyReport report) {
        log.info("📊 Monthly Report for {}", report.getMonth());
        log.info("📚 Books Borrowed: {}", report.getBooksBorrowed().size());
        log.info("📦 Books Returned: {}", report.getBooksReturned().size());
        log.info("⏰ Overdue Books: {}", report.getOverdueBooks().size());

        report.getOverdueBooks().forEach(br ->
                log.info("Overdue → Book={}, User={}, LateFee={}",
                        br.getBook().getTitle(),
                        br.getUser().getEmail(),
                        br.getLateFee()
                )
        );

        report.getUserActivity().forEach(ua ->
                log.info("User={} | Borrowed={} | Returned={}",
                        ua.getEmail(),
                        ua.getBorrowedCount(),
                        ua.getReturnedCount()
                )
        );
    }
}
