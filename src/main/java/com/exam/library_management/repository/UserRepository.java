package com.exam.library_management.repository;

import com.exam.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLibraryId(String libraryId);

    boolean existsByEmail(String email);

    boolean existsByLibraryId(String libraryId);

}
