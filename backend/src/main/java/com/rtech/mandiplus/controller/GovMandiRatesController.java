package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.GovMandiAdviceDTO;
import com.rtech.mandiplus.dto.GovMandiFilterOptionsDTO;
import com.rtech.mandiplus.entity.GovMandiAdvisory;
import com.rtech.mandiplus.entity.GovMandiMsp;
import com.rtech.mandiplus.repository.GovMandiAdvisoryRepository;
import com.rtech.mandiplus.repository.GovMandiMspRepository;
import com.rtech.mandiplus.service.GovMandiAdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mandi-rates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GovMandiRatesController {

    @Autowired
    private GovMandiAdvisorService advisorService;

    @Autowired
    private GovMandiMspRepository mspRepository;

    @Autowired
    private GovMandiAdvisoryRepository advisoryRepository;

    /**
     * Get available districts, markets, and commodities for dropdown selectors.
     */
    @GetMapping("/filter-options")
    public ResponseEntity<GovMandiFilterOptionsDTO> getFilterOptions(
            @RequestParam(required = false, defaultValue = "Maharashtra") String state) {
        return ResponseEntity.ok(advisorService.getFilterOptions(state));
    }

    /**
     * Get latest reported prices, suggested range, trend, verified MSP, and nearby market comparisons.
     */
    @GetMapping("/advice")
    public ResponseEntity<GovMandiAdviceDTO> getMandiAdvice(
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(required = false, defaultValue = "Nashik") String district,
            @RequestParam(required = false, defaultValue = "") String market,
            @RequestParam(required = false, defaultValue = "Tomato") String commodity) {
        return ResponseEntity.ok(advisorService.getMandiAdvice(state, district, market, commodity));
    }

    /**
     * Get verified Government Minimum Support Price (MSP) reference list.
     */
    @GetMapping("/msp")
    public ResponseEntity<List<GovMandiMsp>> getMspList() {
        return ResponseEntity.ok(mspRepository.findAllByOrderByCommodityAsc());
    }

    /**
     * Get active advisories.
     */
    @GetMapping("/advisories")
    public ResponseEntity<List<GovMandiAdvisory>> getActiveAdvisories() {
        return ResponseEntity.ok(advisoryRepository.findByActiveTrueOrderByCreatedAtDesc());
    }
}
