package com.exam.library_management.controller;

import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.dto.ApiResponse;
import com.exam.library_management.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/borrow-records")
@RequiredArgsConstructor
public class AdminBorrowController {

    private final BorrowRecordRepository repository;

    @GetMapping
    public ApiResponse<List<BorrowRecord>> all() {
        // return repository.findAll();
        return new ApiResponse<>(
                true,
                "Borrow records fetched",
                repository.findAll()
        );
    }
}
