package com.exam.library_management.repository;

import org.springframework.data.repository.query.Param;
import com.exam.library_management.dto.UserActivitySummary;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // Active borrow (not returned yet)
    Optional<BorrowRecord> findByUserAndReturnDateIsNull(User user);

    Optional<BorrowRecord> findByBookIdAndReturnDateIsNull(Long bookId);

    List<BorrowRecord> findByUser(User user);

    List<BorrowRecord> findByBorrowDateBetween(LocalDate start, LocalDate end);

    List<BorrowRecord> findByReturnDateBetween(LocalDate start, LocalDate end);

    // @Query("""
    //     SELECT b FROM BorrowRecord b
    //     WHERE b.returnDate IS NULL
    //     AND b.dueDate < :today
    // """)
    // List<BorrowRecord> findOverdueBooks(LocalDate today);
    @Query("""
        SELECT br
        FROM BorrowRecord br
        WHERE br.returnDate IS NULL
          AND br.dueDate < :date
    """)
    List<BorrowRecord> findOverdueBooks(@Param("date") LocalDate date);

    @Query("""
        SELECT new com.exam.library_management.dto.UserActivitySummary(
            u.id,
            u.email,
            SUM(CASE WHEN br.borrowDate BETWEEN :start AND :end THEN 1 ELSE 0 END),
            SUM(CASE WHEN br.returnDate BETWEEN :start AND :end THEN 1 ELSE 0 END)
        )
        FROM BorrowRecord br
        JOIN br.user u
        GROUP BY u.id, u.email
    """)
    List<UserActivitySummary> getUserActivitySummary(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
