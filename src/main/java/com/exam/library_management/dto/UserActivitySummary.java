package com.exam.library_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserActivitySummary {
    private Long userId;
    private String email;
    private long borrowedCount;
    private long returnedCount;
}
