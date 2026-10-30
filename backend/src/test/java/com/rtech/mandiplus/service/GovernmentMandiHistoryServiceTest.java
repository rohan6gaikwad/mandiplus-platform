package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.GovMandiHistoryFilterOptionsDTO;
import com.rtech.mandiplus.dto.GovMandiHistoryResponseDTO;
import com.rtech.mandiplus.dto.GovMandiSyncResultDTO;
import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.repository.GovMandiAuditLogRepository;
import com.rtech.mandiplus.repository.GovMandiSyncLogRepository;
import com.rtech.mandiplus.repository.GovernmentMandiHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GovernmentMandiHistoryServiceTest {

    @Mock
    private GovernmentMandiHistoryRepository historyRepository;

    @Mock
    private GovMandiApiClient apiClient;

    @Mock
    private GovMandiSyncLogRepository syncLogRepository;

    @Mock
    private GovMandiAuditLogRepository auditLogRepository;

    @InjectMocks
    private GovernmentMandiHistoryService historyService;

    private GovMandiPrice priceOct1;
    private GovMandiPrice priceOct2;
    private GovMandiPrice priceOct3;

    @BeforeEach
    void setUp() {
        priceOct1 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ",
                LocalDate.of(2025, 10, 10), new BigDecimal("1200.00"), new BigDecimal("2200.00"), new BigDecimal("1800.00"), "data.gov.in"
        );
        priceOct2 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ",
                LocalDate.of(2025, 10, 15), new BigDecimal("1300.00"), new BigDecimal("2300.00"), new BigDecimal("1900.00"), "data.gov.in"
        );
        priceOct3 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ",
                LocalDate.of(2025, 10, 20), new BigDecimal("1400.00"), new BigDecimal("2400.00"), new BigDecimal("2000.00"), "data.gov.in"
        );
    }

    @Test
    @DisplayName("getMonthlyHistory calculates accurate average modal price, high/low, and daily records")
    void testGetMonthlyHistory_Success() {
        when(historyRepository.findHistoricalPrices(
                eq("Maharashtra"), eq("Nashik"), eq("Lasalgaon"), eq("Onion"),
                eq(LocalDate.of(2025, 10, 1)), eq(LocalDate.of(2025, 10, 31))
        )).thenReturn(List.of(priceOct1, priceOct2, priceOct3));

        when(historyRepository.findAvailableYears(any(), any()))
                .thenReturn(List.of(2026, 2025));

        GovMandiHistoryResponseDTO res = historyService.getMonthlyHistory("Onion", "Maharashtra", "Nashik", "Lasalgaon", 2025, 10);

        assertNotNull(res);
        assertTrue(res.isDataAvailable());
        assertEquals("Onion", res.getCommodity());
        assertEquals("Lasalgaon", res.getMarket());
        assertEquals(3, res.getDailyPrices().size());

        // Average modal price = (1800 + 1900 + 2000) / 3 = 1900.00
        assertEquals(new BigDecimal("1900.00"), res.getSummary().getAverageModalPrice());
        assertEquals(new BigDecimal("19.00"), res.getSummary().getAverageModalPricePerKg());
        assertEquals(new BigDecimal("2000.00"), res.getSummary().getHighestPrice());
        assertEquals(new BigDecimal("1800.00"), res.getSummary().getLowestPrice());
        assertEquals(3, res.getSummary().getTotalTradingDays());
    }

    @Test
    @DisplayName("getYearlyHistory correctly summarizes months and multi-year comparisons")
    void testGetYearlyHistory_Success() {
        when(historyRepository.findHistoricalPrices(
                eq("Maharashtra"), eq("Nashik"), eq("Lasalgaon"), eq("Onion"),
                eq(LocalDate.of(2025, 1, 1)), eq(LocalDate.of(2025, 12, 31))
        )).thenReturn(List.of(priceOct1, priceOct2, priceOct3));

        when(historyRepository.findAvailableYears(any(), any()))
                .thenReturn(List.of(2026, 2025));

        GovMandiHistoryResponseDTO res = historyService.getYearlyHistory("Onion", "Maharashtra", "Nashik", "Lasalgaon", 2025);

        assertNotNull(res);
        assertTrue(res.isDataAvailable());
        assertEquals(1, res.getMonthlyAverages().size());
        assertEquals("October", res.getMonthlyAverages().get(0).getMonthName());
        assertEquals(new BigDecimal("1900.00"), res.getMonthlyAverages().get(0).getAverageModalPrice());
    }

    @Test
    @DisplayName("getDailyHistory returns records for specific date, and handles missing dates truthfully")
    void testGetDailyHistory() {
        LocalDate dateWithData = LocalDate.of(2025, 10, 10);
        when(historyRepository.findDailyPrices("Maharashtra", "Nashik", "Lasalgaon", "Onion", dateWithData))
                .thenReturn(List.of(priceOct1));

        GovMandiHistoryResponseDTO found = historyService.getDailyHistory("Onion", "Maharashtra", "Nashik", "Lasalgaon", dateWithData);
        assertTrue(found.isDataAvailable());
        assertEquals(1, found.getDailyPrices().size());
        assertEquals(new BigDecimal("1800.00"), found.getDailyPrices().get(0).getModalPrice());

        LocalDate emptyDate = LocalDate.of(2025, 10, 11); // Sunday / holiday
        when(historyRepository.findDailyPrices("Maharashtra", "Nashik", "Lasalgaon", "Onion", emptyDate))
                .thenReturn(List.of());

        GovMandiHistoryResponseDTO notFound = historyService.getDailyHistory("Onion", "Maharashtra", "Nashik", "Lasalgaon", emptyDate);
        assertFalse(notFound.isDataAvailable());
        assertTrue(notFound.getStatusMessage().contains("No authentic government market records"));
    }

    @Test
    @DisplayName("getFilterOptions returns available states, districts, markets, and commodities")
    void testGetFilterOptions() {
        when(historyRepository.findDistinctStates()).thenReturn(List.of("Maharashtra"));
        when(historyRepository.findDistinctDistricts("Maharashtra")).thenReturn(List.of("Nashik", "Pune"));
        when(historyRepository.findDistinctCommodities()).thenReturn(List.of("Onion", "Soyabean"));
        when(historyRepository.findDistinctMarkets("Maharashtra", "Nashik")).thenReturn(List.of("Lasalgaon", "Pimpalgaon"));
        when(historyRepository.findDistinctMarkets("Maharashtra", "Pune")).thenReturn(List.of("Pune", "Manchar"));
        when(historyRepository.findAvailableYears("Maharashtra", null)).thenReturn(List.of(2026, 2025));

        GovMandiHistoryFilterOptionsDTO filters = historyService.getFilterOptions("Maharashtra");

        assertNotNull(filters);
        assertEquals(2, filters.getDistricts().size());
        assertEquals(2, filters.getCommodities().size());
        assertTrue(filters.getDistrictMarkets().containsKey("Nashik"));
        assertEquals(2, filters.getDistrictMarkets().get("Nashik").size());
    }

    @Test
    @DisplayName("synchronizeHistoricalData reports missing API key without throwing exception")
    void testSyncWithoutApiKey() {
        when(apiClient.isApiKeyConfigured()).thenReturn(false);

        GovMandiSyncResultDTO result = historyService.synchronizeHistoricalData("Maharashtra", "Nashik", "Lasalgaon", "Onion", null, "admin");

        assertNotNull(result);
        assertEquals("API_KEY_REQUIRED", result.getStatus());
        assertEquals(0, result.getRecordsFetched());
    }

    @Test
    @DisplayName("synchronizeHistoricalData successfully inserts records when API returns data")
    void testSyncSuccess() {
        when(apiClient.isApiKeyConfigured()).thenReturn(true);

        Map<String, Object> rec = Map.of(
                "state", "Maharashtra",
                "district", "Nashik",
                "market", "Lasalgaon",
                "commodity", "Onion",
                "variety", "Red",
                "grade", "FAQ",
                "arrival_date", "25/10/2025",
                "min_price", 1400.0,
                "max_price", 2400.0,
                "modal_price", 1950.0
        );

        GovMandiApiClient.ApiResponse apiResp = new GovMandiApiClient.ApiResponse("ok", 1, 1, "2025-10-25", List.of(rec));
        when(apiClient.fetchMandiRecords("Maharashtra", "Nashik", "Lasalgaon", "Onion", null, 500, 0))
                .thenReturn(apiResp);

        when(historyRepository.findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(Optional.empty());

        GovMandiSyncResultDTO result = historyService.synchronizeHistoricalData("Maharashtra", "Nashik", "Lasalgaon", "Onion", null, "admin");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getRecordsFetched());
        assertEquals(1, result.getRecordsInserted());
        verify(historyRepository, times(1)).save(any(GovMandiPrice.class));
    }
}
