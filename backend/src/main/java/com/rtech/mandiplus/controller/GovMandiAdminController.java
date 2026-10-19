package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.GovMandiAdvisoryDTO;
import com.rtech.mandiplus.dto.GovMandiForecastMetricsDTO;
import com.rtech.mandiplus.dto.GovMandiSyncStatusDTO;
import com.rtech.mandiplus.entity.GovMandiAdvisory;
import com.rtech.mandiplus.entity.GovMandiAuditLog;
import com.rtech.mandiplus.entity.GovMandiMsp;
import com.rtech.mandiplus.entity.GovMandiSyncLog;
import com.rtech.mandiplus.service.GovMandiAdminService;
import com.rtech.mandiplus.service.GovMandiSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/mandi-management")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GovMandiAdminController {

    @Autowired
    private GovMandiAdminService adminService;

    @Autowired
    private GovMandiSyncService syncService;

    @GetMapping({"/status", "/sync-status"})
    public ResponseEntity<GovMandiSyncStatusDTO> getSyncStatus() {
        return ResponseEntity.ok(adminService.getSyncStatus());
    }

    @PostMapping({"/sync", "/trigger-sync"})
    public ResponseEntity<?> triggerManualSync(Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        try {
            GovMandiSyncLog log = syncService.triggerAdminManualSync(adminUsername);
            return ResponseEntity.ok(log);
        } catch (IllegalStateException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "RateLimitExceeded");
            err.put("message", e.getMessage());
            return ResponseEntity.status(429).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "SyncFailed");
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/sync-logs")
    public ResponseEntity<List<GovMandiSyncLog>> getSyncLogs() {
        return ResponseEntity.ok(adminService.getSyncLogs());
    }

    @GetMapping("/forecast-metrics")
    public ResponseEntity<GovMandiForecastMetricsDTO> getForecastMetrics() {
        return ResponseEntity.ok(adminService.getForecastMetrics());
    }

    @GetMapping("/advisories")
    public ResponseEntity<List<GovMandiAdvisory>> getAdvisories() {
        return ResponseEntity.ok(adminService.getAllAdvisories());
    }

    @PostMapping("/advisories")
    public ResponseEntity<GovMandiAdvisory> saveAdvisory(@RequestBody GovMandiAdvisoryDTO dto, Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(adminService.saveAdvisory(dto, adminUsername));
    }

    @PutMapping("/advisories/{id}/toggle")
    public ResponseEntity<?> toggleAdvisory(@PathVariable Long id, Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        adminService.toggleAdvisory(id, adminUsername);
        return ResponseEntity.ok(Map.of("message", "Advisory status toggled"));
    }

    @DeleteMapping("/advisories/{id}")
    public ResponseEntity<?> deleteAdvisory(@PathVariable Long id, Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        adminService.deleteAdvisory(id, adminUsername);
        return ResponseEntity.ok(Map.of("message", "Advisory deleted successfully"));
    }

    @GetMapping("/msp")
    public ResponseEntity<List<GovMandiMsp>> getAllMsp() {
        return ResponseEntity.ok(adminService.getAllMsp());
    }

    @PostMapping("/msp")
    public ResponseEntity<GovMandiMsp> saveMsp(@RequestBody GovMandiMsp msp, Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(adminService.saveMsp(msp, adminUsername));
    }

    @DeleteMapping("/msp/{id}")
    public ResponseEntity<?> deleteMsp(@PathVariable Long id, Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "admin";
        adminService.deleteMsp(id, adminUsername);
        return ResponseEntity.ok(Map.of("message", "MSP reference deleted successfully"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<GovMandiAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
