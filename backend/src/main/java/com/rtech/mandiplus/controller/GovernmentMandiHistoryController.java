package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.GovMandiHistoryFilterOptionsDTO;
import com.rtech.mandiplus.dto.GovMandiHistoryResponseDTO;
import com.rtech.mandiplus.dto.GovMandiSyncResultDTO;
import com.rtech.mandiplus.service.GovernmentMandiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/gov-mandi/history", "/api/mandi-rates/history"})
@CrossOrigin(origins = "*", maxAge = 3600)
public class GovernmentMandiHistoryController {

    @Autowired
    private GovernmentMandiHistoryService historyService;

    /**
     * Get historical Mandi prices for a selected year or custom date range.
     * Suggested: GET /api/gov-mandi/history?commodity=Onion&year=2026
     */
    @GetMapping
    public ResponseEntity<GovMandiHistoryResponseDTO> getHistoricalPrices(
            @RequestParam(required = false, defaultValue = "Onion") String commodity,
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(required = false, defaultValue = "") String district,
            @RequestParam(required = false, defaultValue = "") String market,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(historyService.getDateRangeHistory(commodity, state, district, market, startDate, endDate, null));
        }

        return ResponseEntity.ok(historyService.getYearlyHistory(commodity, state, district, market, year));
    }

    /**
     * Get month-wise historical prices and daily price list.
     * Suggested: GET /api/gov-mandi/history/monthly?commodity=Onion&year=2026&month=10
     */
    @GetMapping("/monthly")
    public ResponseEntity<GovMandiHistoryResponseDTO> getMonthlyPrices(
            @RequestParam(required = false, defaultValue = "Onion") String commodity,
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(required = false, defaultValue = "") String district,
            @RequestParam(required = false, defaultValue = "") String market,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        return ResponseEntity.ok(historyService.getMonthlyHistory(commodity, state, district, market, year, month));
    }

    /**
     * Get day-wise actual recorded mandi price for a particular date.
     * Suggested: GET /api/gov-mandi/history/daily?commodity=Onion&date=2026-10-30
     */
    @GetMapping("/daily")
    public ResponseEntity<GovMandiHistoryResponseDTO> getDailyPrices(
            @RequestParam(required = false, defaultValue = "Onion") String commodity,
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(required = false, defaultValue = "") String district,
            @RequestParam(required = false, defaultValue = "") String market,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(historyService.getDailyHistory(commodity, state, district, market, date));
    }

    /**
     * Get available commodities, states, districts, markets, and recorded years for dropdown selectors.
     * Suggested: GET /api/gov-mandi/history/filters
     */
    @GetMapping("/filters")
    public ResponseEntity<GovMandiHistoryFilterOptionsDTO> getFilters(
            @RequestParam(required = false, defaultValue = "Maharashtra") String state) {

        return ResponseEntity.ok(historyService.getFilterOptions(state));
    }

    /**
     * Trigger synchronization of historical data from the official Government API.
     * POST /api/gov-mandi/history/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<GovMandiSyncResultDTO> syncHistoricalData(
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String arrivalDate,
            Authentication authentication) {

        String triggeredBy = (authentication != null) ? authentication.getName() : "farmer";
        GovMandiSyncResultDTO result = historyService.synchronizeHistoricalData(state, district, market, commodity, arrivalDate, triggeredBy);
        return ResponseEntity.ok(result);
    }
}
