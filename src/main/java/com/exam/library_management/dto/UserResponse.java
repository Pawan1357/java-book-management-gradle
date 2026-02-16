package com.exam.library_management.dto;

import com.exam.library_management.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String libraryId;
    private String email;
    private Role role;
}
