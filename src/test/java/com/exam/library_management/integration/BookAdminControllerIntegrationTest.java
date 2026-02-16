package com.exam.library_management.integration;

import com.exam.library_management.entity.Book;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.BookRepository;
import com.exam.library_management.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookAdminControllerIntegrationTest extends BaseIntegrationTest {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String createAdminAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "admin-book-" + unique + "@test.com";

        User admin = new User();
        admin.setLibraryId("ADMIN" + unique);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        String loginPayload = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, email, PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String token = node.get("data").get("token").asText();
        assertNotNull(token);
        return token;
    }

    private String registerUserAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "user-book-" + unique + "@test.com";
        String libraryId = "USER" + unique;

        String registerPayload = String.format("""
                {
                  "libraryId": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """, libraryId, email, PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        String loginPayload = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, email, PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String token = node.get("data").get("token").asText();
        assertNotNull(token);
        return token;
    }

    private String addPayload(String code, String title, String author) {
        return String.format("""
                {
                  "bookCode": "%s",
                  "title": "%s",
                  "author": "%s"
                }
                """, code, title, author);
    }

    @Test
    void shouldAllowAdminToAddBook() throws Exception {
        String adminToken = createAdminAndLogin();

        mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload("ADM-1001", "Refactoring", "Martin Fowler")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookCode").value("ADM-1001"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }

    @Test
    void shouldReturnBadRequestForDuplicateBookCode() throws Exception {
        String adminToken = createAdminAndLogin();

        mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload("ADM-2001", "Clean Architecture", "Robert C. Martin")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload("ADM-2001", "Duplicate", "Someone")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldAllowAdminToUpdateBook() throws Exception {
        String adminToken = createAdminAndLogin();
        Book book = new Book();
        book.setBookCode("ADM-3001");
        book.setTitle("Old Title");
        book.setAuthor("Old Author");
        book.setStatus(BookStatus.AVAILABLE);
        Book saved = bookRepository.save(book);

        String updatePayload = """
                {
                  "title": "Updated Title",
                  "author": "Updated Author",
                  "status": "BORROWED"
                }
                """;

        mockMvc.perform(put("/api/admin/books/{id}", saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownBook() throws Exception {
        String adminToken = createAdminAndLogin();

        String updatePayload = """
                {
                  "title": "Updated",
                  "author": "Updated",
                  "status": "AVAILABLE"
                }
                """;

        mockMvc.perform(put("/api/admin/books/{id}", 99999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldAllowAdminToDeleteBook() throws Exception {
        String adminToken = createAdminAndLogin();
        Book book = new Book();
        book.setBookCode("ADM-4001");
        book.setTitle("Delete Me");
        book.setAuthor("Author D");
        book.setStatus(BookStatus.AVAILABLE);
        Book saved = bookRepository.save(book);

        mockMvc.perform(delete("/api/admin/books/delete/{id}", saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownBook() throws Exception {
        String adminToken = createAdminAndLogin();

        mockMvc.perform(delete("/api/admin/books/delete/{id}", 99999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldAllowAdminToGetAllBooks() throws Exception {
        String adminToken = createAdminAndLogin();

        Book one = new Book();
        one.setBookCode("ADM-5001");
        one.setTitle("Book One");
        one.setAuthor("A1");
        one.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(one);

        Book two = new Book();
        two.setBookCode("ADM-5002");
        two.setTitle("Book Two");
        two.setAuthor("A2");
        two.setStatus(BookStatus.BORROWED);
        bookRepository.save(two);

        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenForUserRoleOnAdminEndpoints() throws Exception {
        String userToken = registerUserAndLogin();

        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
