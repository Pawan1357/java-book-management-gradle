package com.exam.library_management.integration;

import com.exam.library_management.controller.BorrowController;
import com.exam.library_management.entity.Book;
import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.enums.Role;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.repository.BookRepository;
import com.exam.library_management.repository.BorrowRecordRepository;
import com.exam.library_management.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BorrowControllerIntegrationTest extends BaseIntegrationTest {

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
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BorrowController borrowController;

    @SpyBean
    private com.exam.library_management.service.BorrowService borrowService;

    private String registerUserAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "borrow-user-" + unique + "@test.com";
        String libraryId = "BUSER" + unique;

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

    private String createAdminAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "borrow-admin-" + unique + "@test.com";
        String libraryId = "BADMIN" + unique;

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

    private Book saveAvailableBook(String code, String title, String author) {
        Book book = new Book();
        book.setBookCode(code);
        book.setTitle(title);
        book.setAuthor(author);
        book.setStatus(BookStatus.AVAILABLE);
        return bookRepository.save(book);
    }

    @Test
    void shouldBorrowBookSuccessfullyForUser() throws Exception {
        String userToken = registerUserAndLogin();
        Book book = saveAvailableBook("BORROW-1001", "Domain-Driven Design", "Eric Evans");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.borrowDate").exists())
                .andExpect(jsonPath("$.data.dueDate").exists());

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenUserTriesToBorrowSecondBookWithoutReturningFirst() throws Exception {
        String userToken = registerUserAndLogin();
        Book first = saveAvailableBook("BORROW-2001", "First Book", "Author A");
        Book second = saveAvailableBook("BORROW-2002", "Second Book", "Author B");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", first.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", second.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBookSuccessfullyForUser() throws Exception {
        String userToken = registerUserAndLogin();
        Book book = saveAvailableBook("BORROW-3001", "Returnable Book", "Author R");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/borrow/return")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lateFee").value(BigDecimal.ZERO.intValue()));
    }

    @Test
    void shouldReturnBadRequestWhenReturningWithoutActiveBorrow() throws Exception {
        String userToken = registerUserAndLogin();

        mockMvc.perform(post("/api/user/borrow/return")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldAllowAdminToViewBorrowRecords() throws Exception {
        String userToken = registerUserAndLogin();
        String adminToken = createAdminAndLogin();
        Book book = saveAvailableBook("BORROW-4001", "Admin Records Book", "Author AR");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/borrow-records")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldDenyUserAccessToAdminBorrowRecords() throws Exception {
        String userToken = registerUserAndLogin();

        mockMvc.perform(get("/api/admin/borrow-records")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAdminFromUserBorrowEndpoint() throws Exception {
        String adminToken = createAdminAndLogin();
        Book book = saveAvailableBook("BORROW-5001", "Admin Forbidden Book", "Author AF");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
        Book book = saveAvailableBook("BORROW-6001", "No Token Book", "Author NT");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        Book book = saveAvailableBook("BORROW-7001", "Invalid Token Book", "Author IT");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvidedForReturn() throws Exception {
        mockMvc.perform(post("/api/user/borrow/return"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalidForReturn() throws Exception {
        mockMvc.perform(post("/api/user/borrow/return")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenRoleIsNotUserInsideBorrowControllerGuard() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String adminEmail = "guard-admin-" + unique + "@test.com";

        User admin = new User();
        admin.setLibraryId("GUARD" + unique);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        Book book = saveAvailableBook("BORROW-8001", "Guard Book", "Guard Author");

        mockMvc.perform(post("/api/user/borrow/book/{bookId}", book.getId())
                        .with(user(adminEmail).roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only users can borrow books"));
    }

    @Test
    void shouldReturnBadRequestWhenAuthenticationIsNullInReturnControllerMethod() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> borrowController.returnBook(null)
        );
        assertEquals("Authentication required", ex.getMessage());
    }

    @Test
    void shouldReturnBadRequestWhenAuthenticatedUserNotFoundInReturnControllerMethod() {
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("missing-user@test.com", "n/a");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> borrowController.returnBook(auth)
        );
        assertEquals("Authenticated user not found", ex.getMessage());
    }

    @Test
    void shouldDefaultLateFeeToZeroWhenServiceReturnsNullLateFee() throws Exception {
        String userToken = registerUserAndLogin();

        BorrowRecord mockedRecord = new BorrowRecord();
        mockedRecord.setLateFee(null);

        doReturn(mockedRecord).when(borrowService).returnBook(any(User.class));

        try {
            mockMvc.perform(post("/api/user/borrow/return")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.lateFee").value(BigDecimal.ZERO.intValue()));
        } finally {
            doCallRealMethod().when(borrowService).returnBook(any(User.class));
        }
    }
}
