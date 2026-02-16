package com.exam.library_management.controller;

import com.exam.library_management.dto.BorrowResponse;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.entity.User;
import com.exam.library_management.repository.UserRepository;
import com.exam.library_management.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.exam.library_management.enums.Role;
import com.exam.library_management.exception.BadRequestException;

import java.time.LocalDate;
import java.util.Map;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/user/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    @PostMapping("/book/{bookId}")
    public ApiResponse<Map<String, LocalDate>> borrowBook(
            @PathVariable Long bookId,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        if (user.getRole() != Role.USER) {
                throw new BadRequestException("Only users can borrow books");
        }


        // return borrowService.borrowBook(user, bookId);
        BorrowRecord record = borrowService.borrowBook(user, bookId);
        return new ApiResponse<>(
                true,
                "Book borrowed successfully",
                Map.of(
                        "borrowDate", record.getBorrowDate(),
                        "dueDate", record.getDueDate()
                )
        );
    }

    @PostMapping("/return")
    public ApiResponse<Map<String, Object>> returnBook(Authentication authentication) {

        if (authentication == null) {
                throw new BadRequestException("Authentication required");
        }


        logger.info("Return book API called");

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        // return borrowService.returnBook(user);
        BorrowRecord record = borrowService.returnBook(user);

        BigDecimal lateFee = record.getLateFee() != null ? record.getLateFee() : BigDecimal.ZERO;

        logger.info("Book returned successfully: recordId={}, lateFee={}", record.getId(), lateFee);

        return new ApiResponse<>(
                true,
                "Book returned successfully",
                Map.of("lateFee", lateFee)
        );
    }
//      @PostMapping("/return")
//      public String test() {
//          System.out.println("🔥 RETURN ENDPOINT HIT");
//          return "ok";
//      }
}
