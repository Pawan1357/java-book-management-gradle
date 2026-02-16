package com.exam.library_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
public class TestController {

    @GetMapping("/api/user/hello")
    @PreAuthorize("hasRole('USER')")
    public String userHello() {
        return "Hello USER – JWT is valid";
    }

    @GetMapping("/api/admin/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHello() {
        return "Hello ADMIN – role check passed";
    }
}
