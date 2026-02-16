package com.exam.library_management.integration;

import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
public class UserRepositoryIntegrationTest {

    @Container
    public static final MySQLContainer<?> mysqlContainer =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Test
    void shouldSaveAndFindUserByEmail() {

        User user = new User();
        user.setLibraryId("Repo User");
        user.setEmail("repo@test.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("repo@test.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Repo User", foundUser.get().getLibraryId());
        assertEquals(Role.USER, foundUser.get().getRole());
    }

    @Test
    void shouldCheckExistsByEmail() {

        User user = new User();
        user.setEmail("exists@test.com");
        user.setPassword("password");
        user.setLibraryId("LIB002");
        user.setRole(Role.USER);

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@test.com");

        assertTrue(exists);
    }

    @Test
    void shouldCheckExistsByLibraryId() {

        User user = new User();
        user.setEmail("library@test.com");
        user.setPassword("password");
        user.setLibraryId("LIB003");
        user.setRole(Role.USER);

        userRepository.save(user);

        boolean exists = userRepository.existsByLibraryId("LIB003");

        assertTrue(exists);
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {

        User user1 = new User();
        user1.setLibraryId("User One");
        user1.setEmail("unique@test.com");
        user1.setPassword("password");
        user1.setRole(Role.USER);

        userRepository.save(user1);

        User user2 = new User();
        user2.setLibraryId("User Two");
        user2.setEmail("unique@test.com");
        user2.setPassword("password");
        user2.setRole(Role.USER);

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
