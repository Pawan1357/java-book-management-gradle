package com.exam.library_management;

import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.UserRepository;
import com.exam.library_management.service.UserService;

import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class LibraryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementApplication.class, args);
	}

	@Bean
	@org.springframework.context.annotation.Profile("!test")
	CommandLineRunner testUserService(UserService userService) {
	    return args -> {
		
	        if (!userService.existsByEmail("admin@library.com")) {
	            userService.registerUser(
	                    "LIB001",
	                    "admin@library.com",
	                    "admin123",
	                    Role.ADMIN
	            );
	        }
	
	        if (!userService.existsByEmail("user@library.com")) {
	            userService.registerUser(
	                    "LIB002",
	                    "user@library.com",
	                    "user123",
	                    Role.USER
	            );
	        }
	    };
	}

}
