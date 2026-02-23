package com.exam.library_management.service;
import com.exam.library_management.exception.DuplicateResourceException;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.dto.UserResponse;
import com.exam.library_management.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(String libraryId,
                             String email,
                             String rawPassword,
                             Role role) {

        userRepository.findByEmailOrLibraryId(email, libraryId)
                .ifPresent(existingUser -> {
                    if (existingUser.getEmail().equalsIgnoreCase(email)) {
                        throw new DuplicateResourceException("Email already exists");
                    }
                    if (existingUser.getLibraryId().equals(libraryId)) {
                        throw new DuplicateResourceException("Library ID already exists");
                    }
                });

        try {
            User user = new User();
            user.setLibraryId(libraryId);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            return new UserResponse(
                    savedUser.getLibraryId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

        } catch (DataIntegrityViolationException ex) {
            if (userRepository.existsByLibraryId(libraryId)) {
                throw new DuplicateResourceException("Library ID already exists");
            }
            throw new DuplicateResourceException("Email already exists");
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
