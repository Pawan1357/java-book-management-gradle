package com.exam.library_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/user/hello")
    public String userHello() {
        return "Hello USER – JWT is valid";
    }

    @GetMapping("/api/admin/hello")
    public String adminHello() {
        return "Hello ADMIN – role check passed";
    }
}
