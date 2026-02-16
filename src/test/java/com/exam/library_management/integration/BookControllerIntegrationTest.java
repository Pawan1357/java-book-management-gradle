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

public class BookControllerIntegrationTest extends BaseIntegrationTest {

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

    private String registerUserAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "user-" + unique + "@test.com";
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

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

    private String createAdminAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "admin-" + unique + "@test.com";
        String libraryId = "ADMIN" + unique;

        User admin = new User();
        admin.setLibraryId(libraryId);
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

    private String adminBookPayload(String code, String title, String author) {
        return String.format("""
                {
                  "bookCode": "%s",
                  "title": "%s",
                  "author": "%s"
                }
                """, code, title, author);
    }

    @Test
    void shouldAllowAdminToAddAndListBooks() throws Exception {
        String adminToken = createAdminAndLogin();

        mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBookPayload("BOOK-1001", "Clean Code", "Robert C. Martin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookCode").value("BOOK-1001"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldAllowAdminToUpdateAndDeleteBook() throws Exception {
        String adminToken = createAdminAndLogin();

        MvcResult addResult = mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBookPayload("BOOK-2001", "Old Title", "Old Author")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode added = objectMapper.readTree(addResult.getResponse().getContentAsString());
        long bookId = added.get("data").get("id").asLong();

        String updatePayload = """
                {
                  "title": "New Title",
                  "author": "New Author",
                  "status": "BORROWED"
                }
                """;

        mockMvc.perform(put("/api/admin/books/{id}", bookId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("New Title"))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));

        mockMvc.perform(delete("/api/admin/books/delete/{id}", bookId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldAllowUserToViewOnlyAvailableBooks() throws Exception {
        String userToken = registerUserAndLogin();

        Book available = new Book();
        available.setBookCode("BOOK-3001");
        available.setTitle("Available Book");
        available.setAuthor("Author A");
        available.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(available);

        Book borrowed = new Book();
        borrowed.setBookCode("BOOK-3002");
        borrowed.setTitle("Borrowed Book");
        borrowed.setAuthor("Author B");
        borrowed.setStatus(BookStatus.BORROWED);
        bookRepository.save(borrowed);

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].bookCode").value("BOOK-3001"))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }

    @Test
    void shouldDenyUserAccessToAdminEndpoints() throws Exception {
        String userToken = registerUserAndLogin();

        mockMvc.perform(post("/api/admin/books/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBookPayload("BOOK-4001", "Role Test", "Role Author")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAdminAccessToUserEndpoints() throws Exception {
        String adminToken = createAdminAndLogin();

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }
}
