package com.exam.library_management.controller;

import com.exam.library_management.entity.BorrowRecord;
import com.exam.library_management.repository.BorrowRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBorrowController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 disables security
@ActiveProfiles("test")
class AdminBorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowRecordRepository repository;

    @Test
    @DisplayName("Should fetch all borrow records successfully")
    void getAllBorrowRecords_success() throws Exception {

        BorrowRecord record = new BorrowRecord();

        when(repository.findAll())
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/admin/borrow-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Borrow records fetched"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
