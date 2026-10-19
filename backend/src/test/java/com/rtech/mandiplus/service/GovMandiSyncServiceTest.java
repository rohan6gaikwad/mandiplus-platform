package com.rtech.mandiplus.service;

import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.entity.GovMandiSyncLog;
import com.rtech.mandiplus.repository.GovMandiAuditLogRepository;
import com.rtech.mandiplus.repository.GovMandiPriceRepository;
import com.rtech.mandiplus.repository.GovMandiSyncLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GovMandiSyncServiceTest {

    @Mock
    private GovMandiApiClient apiClient;

    @Mock
    private GovMandiPriceRepository priceRepository;

    @Mock
    private GovMandiSyncLogRepository syncLogRepository;

    @Mock
    private GovMandiAuditLogRepository auditLogRepository;

    @InjectMocks
    private GovMandiSyncService syncService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(syncService, "defaultState", "Maharashtra");
    }

    @Test
    @DisplayName("Test idempotent sync: inserts new records and updates existing records without duplication")
    void testIdempotentSyncAndDuplicatePrevention() {
        // Prepare mock record from API
        Map<String, Object> record = new HashMap<>();
        record.put("state", "Maharashtra");
        record.put("district", "Nashik");
        record.put("market", "Lasalgaon");
        record.put("commodity", "Onion");
        record.put("variety", "Red");
        record.put("grade", "FAQ");
        record.put("arrival_date", "17/10/2026");
        record.put("min_price", "1200");
        record.put("max_price", "2400");
        record.put("modal_price", "1850");

        GovMandiApiClient.ApiResponse apiResponse = new GovMandiApiClient.ApiResponse("ok", 1, 1, "2026-10-17", List.of(record));

        when(apiClient.fetchMandiRecords(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(apiResponse);

        // Case A: Record does not exist yet -> should insert
        when(priceRepository.findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        when(syncLogRepository.save(any(GovMandiSyncLog.class))).thenAnswer(i -> i.getArgument(0));

        GovMandiSyncLog log1 = syncService.executeSync("Maharashtra", null, null, 10, 0, "ADMIN_TEST");

        assertNotNull(log1);
        assertEquals("SUCCESS", log1.getStatus());
        assertEquals(1, log1.getRecordsInserted());
        assertEquals(0, log1.getRecordsUpdated());
        verify(priceRepository, times(1)).save(any(GovMandiPrice.class));

        // Case B: Record already exists -> should update modal price, NOT insert duplicate
        GovMandiPrice existingPrice = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ",
                LocalDate.of(2026, 10, 17), new BigDecimal("1000.00"), new BigDecimal("2000.00"), new BigDecimal("1500.00"), "data.gov.in"
        );
        when(priceRepository.findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(existingPrice));

        GovMandiSyncLog log2 = syncService.executeSync("Maharashtra", null, null, 10, 0, "ADMIN_TEST");

        assertNotNull(log2);
        assertEquals("SUCCESS", log2.getStatus());
        assertEquals(0, log2.getRecordsInserted());
        assertEquals(1, log2.getRecordsUpdated());
        assertEquals(new BigDecimal("1850"), existingPrice.getModalPrice());
    }

    @Test
    @DisplayName("Test rate-limit protection blocks rapid manual sync triggers")
    void testManualSyncCooldownRateLimiting() {
        GovMandiApiClient.ApiResponse emptyResp = new GovMandiApiClient.ApiResponse("ok", 0, 0, "2026-10-17", List.of());
        when(apiClient.fetchMandiRecords(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyResp);
        when(syncLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // First trigger succeeds
        GovMandiSyncLog firstLog = syncService.triggerAdminManualSync("admin");
        assertNotNull(firstLog);

        // Immediate second trigger must throw IllegalStateException (Rate Limit Cooldown)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            syncService.triggerAdminManualSync("admin");
        });

        assertTrue(ex.getMessage().contains("Rate Limit Protection"));
    }
}
