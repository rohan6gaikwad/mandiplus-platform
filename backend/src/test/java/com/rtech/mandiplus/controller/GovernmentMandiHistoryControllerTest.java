package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.config.ProfileCompletionFilter;
import com.rtech.mandiplus.config.SecurityConfig;
import com.rtech.mandiplus.dto.GovMandiHistoryFilterOptionsDTO;
import com.rtech.mandiplus.dto.GovMandiHistoryResponseDTO;
import com.rtech.mandiplus.dto.GovMandiHistorySummaryDTO;
import com.rtech.mandiplus.dto.GovMandiSyncResultDTO;
import com.rtech.mandiplus.repository.GovMandiAdvisoryRepository;
import com.rtech.mandiplus.repository.GovMandiMspRepository;
import com.rtech.mandiplus.security.UserDetailsServiceImpl;
import com.rtech.mandiplus.service.GovernmentMandiHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GovernmentMandiHistoryController.class})
@Import(SecurityConfig.class)
public class GovernmentMandiHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovernmentMandiHistoryService historyService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private GovMandiMspRepository mspRepository;

    @MockBean
    private GovMandiAdvisoryRepository advisoryRepository;

    @Test
    @DisplayName("Unauthenticated request to /api/gov-mandi/history should return 401")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/gov-mandi/history?commodity=Onion&year=2025"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER can access GET /api/gov-mandi/history (yearly analysis)")
    void testGetYearlyHistory() throws Exception {
        GovMandiHistoryResponseDTO dto = new GovMandiHistoryResponseDTO();
        dto.setCommodity("Onion");
        dto.setState("Maharashtra");
        dto.setYear(2025);
        dto.setDataAvailable(true);

        GovMandiHistorySummaryDTO summary = new GovMandiHistorySummaryDTO();
        summary.setAverageModalPrice(new BigDecimal("1950.00"));
        dto.setSummary(summary);

        when(historyService.getYearlyHistory(eq("Onion"), eq("Maharashtra"), eq(""), eq(""), eq(2025)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/gov-mandi/history?commodity=Onion&year=2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commodity").value("Onion"))
                .andExpect(jsonPath("$.summary.averageModalPrice").value(1950.00))
                .andExpect(jsonPath("$.dataAvailable").value(true));
    }

    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER can access GET /api/gov-mandi/history/monthly")
    void testGetMonthlyHistory() throws Exception {
        GovMandiHistoryResponseDTO dto = new GovMandiHistoryResponseDTO();
        dto.setCommodity("Onion");
        dto.setYear(2025);
        dto.setMonth(10);
        dto.setDataAvailable(true);

        when(historyService.getMonthlyHistory(eq("Onion"), eq("Maharashtra"), eq("Nashik"), eq("Lasalgaon"), eq(2025), eq(10)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/gov-mandi/history/monthly?commodity=Onion&district=Nashik&market=Lasalgaon&year=2025&month=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commodity").value("Onion"))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2025));
    }

    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER can access GET /api/gov-mandi/history/daily")
    void testGetDailyHistory() throws Exception {
        GovMandiHistoryResponseDTO dto = new GovMandiHistoryResponseDTO();
        dto.setCommodity("Onion");
        dto.setStartDate(LocalDate.of(2025, 10, 20));
        dto.setDataAvailable(true);

        when(historyService.getDailyHistory(eq("Onion"), eq("Maharashtra"), eq(""), eq(""), eq(LocalDate.of(2025, 10, 20))))
                .thenReturn(dto);

        mockMvc.perform(get("/api/gov-mandi/history/daily?commodity=Onion&date=2025-10-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commodity").value("Onion"))
                .andExpect(jsonPath("$.dataAvailable").value(true));
    }

    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER can access GET /api/gov-mandi/history/filters")
    void testGetFilters() throws Exception {
        GovMandiHistoryFilterOptionsDTO filters = new GovMandiHistoryFilterOptionsDTO(
                "Maharashtra", List.of("Maharashtra"), List.of("Nashik", "Pune"),
                List.of("Onion", "Tomato"), Collections.emptyMap(), List.of(2026, 2025),
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 30), LocalDateTime.now(), 50L
        );

        when(historyService.getFilterOptions("Maharashtra")).thenReturn(filters);

        mockMvc.perform(get("/api/gov-mandi/history/filters?state=Maharashtra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("Maharashtra"))
                .andExpect(jsonPath("$.districts[0]").value("Nashik"));
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("ADMIN can trigger POST /api/gov-mandi/history/sync")
    void testTriggerSync() throws Exception {
        GovMandiSyncResultDTO syncResult = new GovMandiSyncResultDTO(
                "SUCCESS", "Synchronized 15 records", 15, 12, 3, 450L, LocalDateTime.now()
        );

        when(historyService.synchronizeHistoricalData(eq("Maharashtra"), any(), any(), eq("Onion"), any(), eq("admin1")))
                .thenReturn(syncResult);

        mockMvc.perform(post("/api/gov-mandi/history/sync?commodity=Onion&state=Maharashtra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.recordsFetched").value(15));
    }
}
