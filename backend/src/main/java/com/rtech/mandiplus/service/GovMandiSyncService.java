package com.rtech.mandiplus.service;

import com.rtech.mandiplus.entity.GovMandiAuditLog;
import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.entity.GovMandiSyncLog;
import com.rtech.mandiplus.repository.GovMandiAuditLogRepository;
import com.rtech.mandiplus.repository.GovMandiPriceRepository;
import com.rtech.mandiplus.repository.GovMandiSyncLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GovMandiSyncService {

    private static final Logger logger = LoggerFactory.getLogger(GovMandiSyncService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private GovMandiApiClient apiClient;

    @Autowired
    private GovMandiPriceRepository priceRepository;

    @Autowired
    private GovMandiSyncLogRepository syncLogRepository;

    @Autowired
    private GovMandiAuditLogRepository auditLogRepository;

    @Value("${gov.mandi.api.default-state:Maharashtra}")
    private String defaultState;

    private volatile Instant lastSyncExecutionTime = null;
    private static final long MANUAL_COOLDOWN_SECONDS = 60; // 1 minute cooldown to prevent API throttling

    /**
     * Daily scheduled sync for official Mandi prices.
     */
    @Scheduled(cron = "${gov.mandi.api.sync.cron:0 0 4 * * ?}")
    public void scheduledDailySync() {
        logger.info("Executing scheduled daily sync for Government Mandi Prices (State: {})...", defaultState);
        try {
            executeSync(defaultState, null, null, 1000, 0, "SCHEDULED_JOB");
        } catch (Exception e) {
            logger.error("Scheduled sync encountered an issue: {}", e.getMessage());
        }
    }

    /**
     * Admin-triggered manual sync with rate-limit protection.
     */
    public GovMandiSyncLog triggerAdminManualSync(String adminUsername) {
        if (lastSyncExecutionTime != null) {
            long secondsSinceLast = Instant.now().getEpochSecond() - lastSyncExecutionTime.getEpochSecond();
            if (secondsSinceLast < MANUAL_COOLDOWN_SECONDS) {
                long waitRemaining = MANUAL_COOLDOWN_SECONDS - secondsSinceLast;
                throw new IllegalStateException("Rate Limit Protection: A government sync was executed " + secondsSinceLast +
                        "s ago. Please wait " + waitRemaining + "s before requesting another sync to respect data.gov.in limits.");
            }
        }

        lastSyncExecutionTime = Instant.now();
        GovMandiSyncLog log = executeSync(defaultState, null, null, 500, 0, adminUsername);

        auditLogRepository.save(new GovMandiAuditLog(
                "TRIGGER_MANUAL_SYNC",
                adminUsername,
                "Manual synchronization executed for State: " + defaultState + ". Records inserted: " +
                        log.getRecordsInserted() + ", updated: " + log.getRecordsUpdated()
        ));

        return log;
    }

    @Transactional
    public GovMandiSyncLog executeSync(String state, String district, String commodity, int limit, int offset, String triggeredBy) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int updated = 0;
        int fetched = 0;

        try {
            GovMandiApiClient.ApiResponse response = apiClient.fetchMandiRecords(state, district, commodity, limit, offset);
            List<Map<String, Object>> records = response.getRecords();
            fetched = records.size();

            for (Map<String, Object> rec : records) {
                String recState = (String) rec.getOrDefault("state", defaultState);
                String recDistrict = (String) rec.getOrDefault("district", "");
                String recMarket = (String) rec.getOrDefault("market", "");
                String recCommodity = (String) rec.getOrDefault("commodity", "");
                String recVariety = (String) rec.getOrDefault("variety", "Other");
                String recGrade = (String) rec.getOrDefault("grade", "FAQ");
                String dateStr = (String) rec.getOrDefault("arrival_date", "");

                LocalDate arrivalDate = parseArrivalDate(dateStr);
                if (arrivalDate == null || recCommodity.isEmpty() || recMarket.isEmpty()) {
                    continue;
                }

                BigDecimal minPrice = parsePrice(rec.get("min_price"));
                BigDecimal maxPrice = parsePrice(rec.get("max_price"));
                BigDecimal modalPrice = parsePrice(rec.get("modal_price"));

                Optional<GovMandiPrice> existing = priceRepository.findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
                        recState, recDistrict, recMarket, recCommodity, recVariety, recGrade, arrivalDate
                );

                if (existing.isPresent()) {
                    GovMandiPrice price = existing.get();
                    price.setMinPrice(minPrice);
                    price.setMaxPrice(maxPrice);
                    price.setModalPrice(modalPrice);
                    price.setImportedAt(LocalDateTime.now());
                    priceRepository.save(price);
                    updated++;
                } else {
                    GovMandiPrice newPrice = new GovMandiPrice(
                            recState, recDistrict, recMarket, recCommodity, recVariety, recGrade, arrivalDate,
                            minPrice, maxPrice, modalPrice, "data.gov.in (Agmarknet)"
                    );
                    priceRepository.save(newPrice);
                    inserted++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            GovMandiSyncLog syncLog = new GovMandiSyncLog(
                    "SUCCESS", fetched, inserted, updated, null, triggeredBy, duration
            );
            return syncLogRepository.save(syncLog);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown sync error";
            logger.error("Mandi synchronization failed: {}", errorMsg);

            GovMandiSyncLog failLog = new GovMandiSyncLog(
                    "FAILED", fetched, inserted, updated, errorMsg, triggeredBy, duration
            );
            return syncLogRepository.save(failLog);
        }
    }

    private LocalDate parseArrivalDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(raw.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw.trim());
            } catch (Exception ex) {
                logger.warn("Could not parse arrival date: '{}'", raw);
                return null;
            }
        }
    }

    private BigDecimal parsePrice(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.toString().trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
