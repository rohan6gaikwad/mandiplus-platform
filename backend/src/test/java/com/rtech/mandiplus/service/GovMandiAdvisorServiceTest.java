package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.GovMandiAdviceDTO;
import com.rtech.mandiplus.dto.NearbyMarketPriceDTO;
import com.rtech.mandiplus.entity.GovMandiMsp;
import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.repository.GovMandiMspRepository;
import com.rtech.mandiplus.repository.GovMandiPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GovMandiAdvisorServiceTest {

    @Mock
    private GovMandiPriceRepository priceRepository;

    @Mock
    private GovMandiMspRepository mspRepository;

    @Mock
    private com.rtech.mandiplus.repository.GovMandiAdvisoryRepository advisoryRepository;

    @InjectMocks
    private GovMandiAdvisorService advisorService;

    private GovMandiPrice priceDay1;
    private GovMandiPrice priceDay2;
    private GovMandiPrice priceDay3;
    private GovMandiMsp verifiedMsp;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        priceDay1 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ",
                today.minusDays(2), new BigDecimal("4600.00"), new BigDecimal("4900.00"), new BigDecimal("4750.00"), "data.gov.in"
        );

        priceDay2 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ",
                today.minusDays(1), new BigDecimal("4700.00"), new BigDecimal("5000.00"), new BigDecimal("4850.00"), "data.gov.in"
        );

        priceDay3 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ",
                today, new BigDecimal("4800.00"), new BigDecimal("5100.00"), new BigDecimal("4950.00"), "data.gov.in"
        );

        verifiedMsp = new GovMandiMsp("Soyabean", "Kharif", new BigDecimal("4892.00"), "2024-25", "Ministry of Agriculture (Verified)");
    }

    @Test
    @DisplayName("Test accurate moving average and rising trend calculation with verified MSP")
    void testGetPriceAdvice_RisingTrendAndSufficientData() {
        when(priceRepository.findHistoryByMarketAndCommodity(eq("Maharashtra"), eq("Nashik"), eq("Lasalgaon"), eq("Soyabean"), any()))
                .thenReturn(List.of(priceDay3, priceDay2, priceDay1));

        when(mspRepository.findByCommodityIgnoreCase("Soyabean"))
                .thenReturn(Optional.of(verifiedMsp));

        GovMandiAdviceDTO advice = advisorService.getMandiAdvice("Maharashtra", "Nashik", "Lasalgaon", "Soyabean");

        assertNotNull(advice);
        assertEquals("Soyabean", advice.getCommodity());
        assertEquals("Lasalgaon", advice.getMarket());
        assertEquals(new BigDecimal("4950.00"), advice.getModalPrice());

        // Trend should be RISING (4950 > 4750 * 1.01)
        assertEquals("RISING", advice.getTrend());

        // Moving average: (4950 + 4850 + 4750) / 3 = 4850.00
        assertEquals(new BigDecimal("4850.00"), advice.getMovingAveragePrice());

        // Suggested range must exist and be reasonable
        assertTrue(advice.getHasSuggestedRange());
        assertNotNull(advice.getSuggestedMinPrice());
        assertNotNull(advice.getSuggestedMaxPrice());
        assertTrue(advice.getSuggestedMinPrice().compareTo(advice.getSuggestedMaxPrice()) <= 0);

        // Verified MSP must be present
        assertTrue(advice.getIsMspVerified());
        assertEquals(new BigDecimal("4892.00"), advice.getMspPrice());

        // Crucial distinction: modal price must not equal MSP
        assertNotEquals(advice.getModalPrice(), advice.getMspPrice());
        assertFalse(advice.getIsStale());
    }

    @Test
    @DisplayName("Test stale data warning when latest arrival is older than 3 days")
    void testGetPriceAdvice_StaleDataWarning() {
        GovMandiPrice oldPrice = new GovMandiPrice(
                "Maharashtra", "Nashik", "Niphad", "Wheat", "Lokwan", "FAQ",
                LocalDate.now().minusDays(5), new BigDecimal("2200.00"), new BigDecimal("2400.00"), new BigDecimal("2300.00"), "data.gov.in"
        );

        when(priceRepository.findHistoryByMarketAndCommodity(eq("Maharashtra"), eq("Nashik"), eq("Niphad"), eq("Wheat"), any()))
                .thenReturn(List.of(oldPrice));
        when(mspRepository.findByCommodityIgnoreCase("Wheat"))
                .thenReturn(Optional.empty());

        GovMandiAdviceDTO advice = advisorService.getMandiAdvice("Maharashtra", "Nashik", "Niphad", "Wheat");

        assertNotNull(advice);
        assertTrue(advice.getIsStale());
        assertNotNull(advice.getStaleWarning());
        assertTrue(advice.getStaleWarning().contains("over 3 days old") || advice.getStaleWarning().contains("stale") || advice.getStaleWarning().contains("days"));
    }

    @Test
    @DisplayName("Test insufficient data handling without fabricating prices")
    void testGetPriceAdvice_InsufficientData() {
        when(priceRepository.findHistoryByMarketAndCommodity(eq("Maharashtra"), eq("Nashik"), eq("Yeola"), eq("Tomato"), any()))
                .thenReturn(List.of(priceDay3)); // only 1 record
        when(mspRepository.findByCommodityIgnoreCase("Tomato"))
                .thenReturn(Optional.empty());

        GovMandiAdviceDTO advice = advisorService.getMandiAdvice("Maharashtra", "Nashik", "Yeola", "Tomato");

        assertNotNull(advice);
        assertFalse(advice.getHasSuggestedRange());
        assertEquals("NOT_ENOUGH_DATA", advice.getTrend());
        assertNull(advice.getSuggestedMinPrice());
        assertNull(advice.getSuggestedMaxPrice());
    }

    @Test
    @DisplayName("Test nearby mandi comparisons")
    void testGetNearbyMarketPrices() {
        GovMandiPrice nearby1 = new GovMandiPrice(
                "Maharashtra", "Nashik", "Pimpalgaon", "Soyabean", "Yellow", "FAQ",
                LocalDate.now(), new BigDecimal("4700.00"), new BigDecimal("5050.00"), new BigDecimal("4900.00"), "data.gov.in"
        );

        when(priceRepository.findHistoryByMarketAndCommodity(any(), any(), any(), any(), any()))
                .thenReturn(List.of(priceDay3, priceDay2));
        when(priceRepository.findDistrictPricesByCommodity(eq("Maharashtra"), eq("Nashik"), eq("Soyabean"), eq("Lasalgaon"), any()))
                .thenReturn(List.of(nearby1));

        GovMandiAdviceDTO advice = advisorService.getMandiAdvice("Maharashtra", "Nashik", "Lasalgaon", "Soyabean");

        assertNotNull(advice);
        assertNotNull(advice.getNearbyMarkets());
        assertFalse(advice.getNearbyMarkets().isEmpty());
        assertEquals("Pimpalgaon", advice.getNearbyMarkets().get(0).getMarket());
        assertEquals(new BigDecimal("4900.00"), advice.getNearbyMarkets().get(0).getModalPrice());
    }
}
