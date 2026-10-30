package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.GovMandiAuditLog;
import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.entity.GovMandiSyncLog;
import com.rtech.mandiplus.repository.GovMandiAuditLogRepository;
import com.rtech.mandiplus.repository.GovMandiSyncLogRepository;
import com.rtech.mandiplus.repository.GovernmentMandiHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GovernmentMandiHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(GovernmentMandiHistoryService.class);
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private GovernmentMandiHistoryRepository historyRepository;

    @Autowired
    private GovMandiApiClient apiClient;

    @Autowired
    private GovMandiSyncLogRepository syncLogRepository;

    @Autowired
    private GovMandiAuditLogRepository auditLogRepository;

    @Value("${gov.mandi.api.default-state:Maharashtra}")
    private String defaultState;

    /**
     * Get complete historical analysis for a selected year with monthly breakdowns and multi-year comparison.
     */
    public GovMandiHistoryResponseDTO getYearlyHistory(String commodity, String state, String district, String market, Integer year) {
        String targetCommodity = cleanParam(commodity, "Onion");
        String targetState = cleanParam(state, defaultState);
        String targetDistrict = cleanParam(district, "");
        String targetMarket = cleanParam(market, "");

        int targetYear = (year != null && year > 2000) ? year : LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(targetYear, 1, 1);
        LocalDate endDate = LocalDate.of(targetYear, 12, 31);

        List<GovMandiPrice> records = historyRepository.findHistoricalPrices(
                targetState, targetDistrict, targetMarket, targetCommodity, startDate, endDate
        );

        GovMandiHistoryResponseDTO response = buildBaseResponse(targetCommodity, targetState, targetDistrict, targetMarket, "YEAR");
        response.setYear(targetYear);
        response.setStartDate(startDate);
        response.setEndDate(endDate);

        if (records.isEmpty()) {
            response.setDataAvailable(false);
            response.setStatusMessage("No authentic government market records found for " + targetCommodity +
                    (targetMarket.isEmpty() ? "" : " in " + targetMarket) +
                    (targetDistrict.isEmpty() ? "" : ", " + targetDistrict) + " for year " + targetYear + ".");
            response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
            return response;
        }

        // 1. Process daily prices
        List<DailyMandiPriceDTO> dailyList = records.stream()
                .map(this::mapToDailyDTO)
                .collect(Collectors.toList());
        response.setDailyPrices(dailyList);

        // 2. Compute monthly summaries (Jan - Dec)
        Map<Integer, List<GovMandiPrice>> byMonth = records.stream()
                .collect(Collectors.groupingBy(p -> p.getArrivalDate().getMonthValue()));

        List<MonthlyPriceSummaryDTO> monthlyList = new ArrayList<>();
        BigDecimal previousMonthAvg = null;

        for (int m = 1; m <= 12; m++) {
            List<GovMandiPrice> monthRecs = byMonth.getOrDefault(m, Collections.emptyList());
            if (!monthRecs.isEmpty()) {
                MonthlyPriceSummaryDTO mDto = computeMonthlySummary(targetYear, m, monthRecs, previousMonthAvg);
                monthlyList.add(mDto);
                previousMonthAvg = mDto.getAverageModalPrice();
            }
        }
        response.setMonthlyAverages(monthlyList);

        // 3. Compute overall yearly summary
        GovMandiHistorySummaryDTO summary = computePeriodSummary(records);
        response.setSummary(summary);

        // 4. Compute multi-year comparison for this commodity
        response.setYearlyAverages(computeMultiYearComparison(targetCommodity, targetState, targetDistrict, targetMarket));
        response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
        response.setDataAvailable(true);
        response.setStatusMessage("Retrieved " + records.size() + " official Government APMC arrival records for " + targetCommodity + " (" + targetYear + ").");

        return response;
    }

    /**
     * Get daily mandi prices for a selected month, with monthly average, highest/lowest price,
     * and month-to-month / year-to-year comparisons.
     */
    public GovMandiHistoryResponseDTO getMonthlyHistory(String commodity, String state, String district, String market, Integer year, Integer month) {
        String targetCommodity = cleanParam(commodity, "Onion");
        String targetState = cleanParam(state, defaultState);
        String targetDistrict = cleanParam(district, "");
        String targetMarket = cleanParam(market, "");

        int targetYear = (year != null && year > 2000) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : LocalDate.now().getMonthValue();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<GovMandiPrice> records = historyRepository.findHistoricalPrices(
                targetState, targetDistrict, targetMarket, targetCommodity, startDate, endDate
        );

        GovMandiHistoryResponseDTO response = buildBaseResponse(targetCommodity, targetState, targetDistrict, targetMarket, "MONTH");
        response.setYear(targetYear);
        response.setMonth(targetMonth);
        response.setStartDate(startDate);
        response.setEndDate(endDate);

        if (records.isEmpty()) {
            response.setDataAvailable(false);
            response.setStatusMessage("No authentic government market records found for " + targetCommodity +
                    (targetMarket.isEmpty() ? "" : " in " + targetMarket) + " for " +
                    Month.of(targetMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + targetYear + ".");
            response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
            return response;
        }

        // 1. Process daily prices (sorted ascending for charting)
        List<GovMandiPrice> sortedAsc = new ArrayList<>(records);
        sortedAsc.sort(Comparator.comparing(GovMandiPrice::getArrivalDate));
        List<DailyMandiPriceDTO> dailyList = sortedAsc.stream()
                .map(this::mapToDailyDTO)
                .collect(Collectors.toList());
        response.setDailyPrices(dailyList);

        // 2. Monthly Summary
        GovMandiHistorySummaryDTO summary = computePeriodSummary(records);

        // Calculate comparison with previous month
        YearMonth prevYm = ym.minusMonths(1);
        List<GovMandiPrice> prevMonthRecs = historyRepository.findHistoricalPrices(
                targetState, targetDistrict, targetMarket, targetCommodity, prevYm.atDay(1), prevYm.atEndOfMonth()
        );
        if (!prevMonthRecs.isEmpty()) {
            BigDecimal prevAvg = calculateAverageModal(prevMonthRecs);
            if (prevAvg != null && prevAvg.compareTo(BigDecimal.ZERO) > 0) {
                double diffPercent = (summary.getAverageModalPrice().doubleValue() - prevAvg.doubleValue()) / prevAvg.doubleValue() * 100.0;
                double roundedDiff = Math.round(diffPercent * 10.0) / 10.0;
                summary.setPriceChangePercentage(roundedDiff);
                if (roundedDiff > 1.0) {
                    summary.setTrend("RISING");
                    summary.setComparisonText("+" + roundedDiff + "% compared to " + prevYm.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + prevYm.getYear());
                } else if (roundedDiff < -1.0) {
                    summary.setTrend("FALLING");
                    summary.setComparisonText(roundedDiff + "% compared to " + prevYm.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + prevYm.getYear());
                } else {
                    summary.setTrend("STABLE");
                    summary.setComparisonText("Stable (0.0% variance) compared to " + prevYm.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + prevYm.getYear());
                }
            }
        }

        response.setSummary(summary);

        // 3. Single Month Summary item in monthlyAverages
        MonthlyPriceSummaryDTO singleMonthSummary = computeMonthlySummary(targetYear, targetMonth, records, null);
        singleMonthSummary.setMonthOverMonthChangePercentage(summary.getPriceChangePercentage());
        singleMonthSummary.setTrendDirection(summary.getTrend());
        response.setMonthlyAverages(Collections.singletonList(singleMonthSummary));

        // 4. Same month across different years comparison
        response.setYearlyAverages(computeSameMonthAcrossYears(targetCommodity, targetState, targetDistrict, targetMarket, targetMonth));
        response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
        response.setDataAvailable(true);
        response.setStatusMessage("Showing " + records.size() + " official daily arrivals for " +
                Month.of(targetMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + targetYear + ".");

        return response;
    }

    /**
     * Get day-wise prices for a specific date, with market details, min, max, and modal prices.
     */
    public GovMandiHistoryResponseDTO getDailyHistory(String commodity, String state, String district, String market, LocalDate date) {
        String targetCommodity = cleanParam(commodity, "Onion");
        String targetState = cleanParam(state, defaultState);
        String targetDistrict = cleanParam(district, "");
        String targetMarket = cleanParam(market, "");

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        List<GovMandiPrice> records = historyRepository.findDailyPrices(
                targetState, targetDistrict, targetMarket, targetCommodity, targetDate
        );

        GovMandiHistoryResponseDTO response = buildBaseResponse(targetCommodity, targetState, targetDistrict, targetMarket, "DAY");
        response.setStartDate(targetDate);
        response.setEndDate(targetDate);
        response.setYear(targetDate.getYear());
        response.setMonth(targetDate.getMonthValue());

        if (records.isEmpty()) {
            response.setDataAvailable(false);
            response.setStatusMessage("No authentic government market records reported for " + targetCommodity +
                    " on " + targetDate.format(DISPLAY_DATE_FORMAT) + ". (Mandis are closed on holidays, Sundays, or days without auction arrivals).");
            response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
            return response;
        }

        List<DailyMandiPriceDTO> dailyList = records.stream()
                .map(this::mapToDailyDTO)
                .collect(Collectors.toList());
        response.setDailyPrices(dailyList);
        response.setSummary(computePeriodSummary(records));
        response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
        response.setDataAvailable(true);
        response.setStatusMessage("Found " + records.size() + " official market auction report(s) on " + targetDate.format(DISPLAY_DATE_FORMAT) + ".");

        return response;
    }

    /**
     * Get historical records for a flexible date range with pagination.
     */
    public GovMandiHistoryResponseDTO getDateRangeHistory(String commodity, String state, String district, String market,
                                                          LocalDate startDate, LocalDate endDate, Pageable pageable) {
        String targetCommodity = cleanParam(commodity, "Onion");
        String targetState = cleanParam(state, defaultState);
        String targetDistrict = cleanParam(district, "");
        String targetMarket = cleanParam(market, "");

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusMonths(6);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        Pageable effectivePageable = (pageable != null) ? pageable : PageRequest.of(0, 100);
        Page<GovMandiPrice> page = historyRepository.findHistoricalPricesPage(
                targetState, targetDistrict, targetMarket, targetCommodity, start, end, effectivePageable
        );

        GovMandiHistoryResponseDTO response = buildBaseResponse(targetCommodity, targetState, targetDistrict, targetMarket, "CUSTOM");
        response.setStartDate(start);
        response.setEndDate(end);

        List<GovMandiPrice> records = page.getContent();
        if (records.isEmpty()) {
            response.setDataAvailable(false);
            response.setStatusMessage("No official arrival records found between " + start.format(DISPLAY_DATE_FORMAT) +
                    " and " + end.format(DISPLAY_DATE_FORMAT) + ".");
            response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
            return response;
        }

        List<DailyMandiPriceDTO> dailyList = records.stream()
                .map(this::mapToDailyDTO)
                .collect(Collectors.toList());
        response.setDailyPrices(dailyList);
        response.setSummary(computePeriodSummary(records));
        response.setAvailableYears(getAvailableYears(targetState, targetCommodity));
        response.setDataAvailable(true);
        response.setStatusMessage("Showing " + records.size() + " of " + page.getTotalElements() + " total official records.");

        return response;
    }

    /**
     * Get dynamic dropdown filters: states, districts, markets by district, commodities, and available years.
     */
    public GovMandiHistoryFilterOptionsDTO getFilterOptions(String state) {
        String targetState = cleanParam(state, defaultState);

        List<String> states = historyRepository.findDistinctStates();
        if (states.isEmpty() || !states.contains("Maharashtra")) {
            states = new ArrayList<>(states);
            if (!states.contains("Maharashtra")) states.add(0, "Maharashtra");
        }

        List<String> districts = historyRepository.findDistinctDistricts(targetState);
        List<String> commodities = historyRepository.findDistinctCommodities();

        Map<String, List<String>> districtMarkets = new HashMap<>();
        for (String dist : districts) {
            List<String> markets = historyRepository.findDistinctMarkets(targetState, dist);
            districtMarkets.put(dist, markets);
        }

        // Fallback reference data if DB is initially sparse
        if (districts.isEmpty()) {
            districts = Arrays.asList("Nashik", "Ahilyanagar", "Pune", "Kolhapur", "Nagpur", "Solapur", "Chhatrapati Sambhajinagar", "Jalgaon");
            for (String dist : districts) {
                districtMarkets.put(dist, Collections.singletonList("APMC " + dist + " Yard"));
            }
        }
        if (commodities.isEmpty()) {
            commodities = Arrays.asList("Onion", "Soyabean", "Tomato", "Wheat", "Cotton", "Maize", "Gram", "Potato", "Grapes");
        }

        List<Integer> years = historyRepository.findAvailableYears(targetState, null);
        if (years.isEmpty()) {
            years = Arrays.asList(LocalDate.now().getYear(), LocalDate.now().getYear() - 1);
        }

        LocalDate minDate = historyRepository.findMinDateByCommodity("Onion");
        LocalDate maxDate = historyRepository.findMaxDateByCommodity("Onion");
        LocalDateTime lastSync = historyRepository.findLatestImportedAt();
        long totalRecords = historyRepository.count();

        return new GovMandiHistoryFilterOptionsDTO(
                targetState, states, districts, commodities, districtMarkets, years,
                minDate, maxDate, lastSync, totalRecords
        );
    }

    /**
     * Synchronize historical records directly from the Government of India Open Government Data (OGD) API.
     * Upserts into TiDB database and logs audit history.
     */
    @Transactional
    public GovMandiSyncResultDTO synchronizeHistoricalData(String state, String district, String market, String commodity, String arrivalDate, String triggeredBy) {
        String targetState = cleanParam(state, defaultState);
        String targetDistrict = cleanParam(district, null);
        String targetMarket = cleanParam(market, null);
        String targetCommodity = cleanParam(commodity, null);
        String targetArrivalDate = cleanParam(arrivalDate, null);

        long startTime = System.currentTimeMillis();
        int fetched = 0;
        int inserted = 0;
        int updated = 0;

        try {
            if (!apiClient.isApiKeyConfigured()) {
                long duration = System.currentTimeMillis() - startTime;
                return new GovMandiSyncResultDTO(
                        "API_KEY_REQUIRED",
                        "DATA_GOV_IN_API_KEY environment variable is not configured. Historical data synchronization from data.gov.in requires a valid API key.",
                        0, 0, 0, duration, LocalDateTime.now()
                );
            }

            GovMandiApiClient.ApiResponse response = apiClient.fetchMandiRecords(
                    targetState, targetDistrict, targetMarket, targetCommodity, targetArrivalDate, 500, 0
            );

            List<Map<String, Object>> records = response.getRecords();
            fetched = records.size();

            for (Map<String, Object> rec : records) {
                String recState = (String) rec.getOrDefault("state", targetState);
                String recDistrict = (String) rec.getOrDefault("district", "");
                String recMarket = (String) rec.getOrDefault("market", "");
                String recCommodity = (String) rec.getOrDefault("commodity", "");
                String recVariety = (String) rec.getOrDefault("variety", "Other");
                String recGrade = (String) rec.getOrDefault("grade", "FAQ");
                String dateStr = (String) rec.getOrDefault("arrival_date", "");

                LocalDate recArrivalDate = parseDate(dateStr);
                if (recArrivalDate == null || recCommodity.isEmpty() || recMarket.isEmpty()) {
                    continue;
                }

                BigDecimal minPrice = parsePrice(rec.get("min_price"));
                BigDecimal maxPrice = parsePrice(rec.get("max_price"));
                BigDecimal modalPrice = parsePrice(rec.get("modal_price"));

                Optional<GovMandiPrice> existing = historyRepository.findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
                        recState, recDistrict, recMarket, recCommodity, recVariety, recGrade, recArrivalDate
                );

                if (existing.isPresent()) {
                    GovMandiPrice p = existing.get();
                    p.setMinPrice(minPrice);
                    p.setMaxPrice(maxPrice);
                    p.setModalPrice(modalPrice);
                    p.setImportedAt(LocalDateTime.now());
                    historyRepository.save(p);
                    updated++;
                } else {
                    GovMandiPrice newPrice = new GovMandiPrice(
                            recState, recDistrict, recMarket, recCommodity, recVariety, recGrade, recArrivalDate,
                            minPrice, maxPrice, modalPrice, "data.gov.in (Agmarknet)"
                    );
                    historyRepository.save(newPrice);
                    inserted++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            GovMandiSyncLog syncLog = new GovMandiSyncLog("SUCCESS", fetched, inserted, updated, null, triggeredBy, duration);
            syncLogRepository.save(syncLog);

            auditLogRepository.save(new GovMandiAuditLog(
                    "SYNC_HISTORICAL_DATA",
                    triggeredBy != null ? triggeredBy : "SYSTEM",
                    "Synchronized official Government Mandi history. Fetched: " + fetched + ", Inserted: " + inserted + ", Updated: " + updated
            ));

            return new GovMandiSyncResultDTO(
                    "SUCCESS",
                    "Successfully synchronized " + fetched + " official records from data.gov.in (Agmarknet). Inserted: " + inserted + ", Updated: " + updated + ".",
                    fetched, inserted, updated, duration, LocalDateTime.now()
            );

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown sync error";
            logger.error("Failed to sync historical government data: {}", errorMsg);

            GovMandiSyncLog failLog = new GovMandiSyncLog("FAILED", fetched, inserted, updated, errorMsg, triggeredBy, duration);
            syncLogRepository.save(failLog);

            return new GovMandiSyncResultDTO("FAILED", "Synchronization failed: " + errorMsg, fetched, inserted, updated, duration, LocalDateTime.now());
        }
    }

    // ==========================================
    // Internal Helper Calculations
    // ==========================================

    private GovMandiHistoryResponseDTO buildBaseResponse(String commodity, String state, String district, String market, String viewMode) {
        GovMandiHistoryResponseDTO res = new GovMandiHistoryResponseDTO();
        res.setCommodity(commodity);
        res.setState(state);
        res.setDistrict(district);
        res.setMarket(market);
        res.setViewMode(viewMode);
        res.setDataSource("Official Government of India Open Government Data (OGD) / Agmarknet (data.gov.in)");
        res.setResourceId(apiClient.getResourceId());
        res.setApiKeyConfigured(apiClient.isApiKeyConfigured());
        res.setLastSyncTimestamp(historyRepository.findLatestImportedAt());
        return res;
    }

    private DailyMandiPriceDTO mapToDailyDTO(GovMandiPrice p) {
        BigDecimal perKg = (p.getModalPrice() != null)
                ? p.getModalPrice().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal spread = (p.getMaxPrice() != null && p.getMinPrice() != null)
                ? p.getMaxPrice().subtract(p.getMinPrice())
                : BigDecimal.ZERO;

        return new DailyMandiPriceDTO(
                p.getId(), p.getState(), p.getDistrict(), p.getMarket(), p.getCommodity(),
                p.getVariety(), p.getGrade(), p.getArrivalDate(),
                p.getArrivalDate().format(DISPLAY_DATE_FORMAT),
                p.getMinPrice(), p.getMaxPrice(), p.getModalPrice(),
                perKg, spread, p.getRawSource(), p.getImportedAt()
        );
    }

    private MonthlyPriceSummaryDTO computeMonthlySummary(int year, int month, List<GovMandiPrice> monthRecs, BigDecimal prevMonthAvg) {
        BigDecimal avgModal = calculateAverageModal(monthRecs);
        BigDecimal avgPerKg = avgModal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal minPrice = monthRecs.stream().map(GovMandiPrice::getMinPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = monthRecs.stream().map(GovMandiPrice::getMaxPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal highModal = monthRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowModal = monthRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        int tradingDays = (int) monthRecs.stream().map(GovMandiPrice::getArrivalDate).distinct().count();

        Double changePercent = null;
        String trend = "STABLE";
        if (prevMonthAvg != null && prevMonthAvg.compareTo(BigDecimal.ZERO) > 0) {
            double diff = (avgModal.doubleValue() - prevMonthAvg.doubleValue()) / prevMonthAvg.doubleValue() * 100.0;
            changePercent = Math.round(diff * 10.0) / 10.0;
            if (changePercent > 1.0) trend = "RISING";
            else if (changePercent < -1.0) trend = "FALLING";
        }

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return new MonthlyPriceSummaryDTO(
                year, month, monthName, avgModal, avgPerKg, minPrice, maxPrice, highModal, lowModal,
                monthRecs.size(), tradingDays, changePercent, trend
        );
    }

    private GovMandiHistorySummaryDTO computePeriodSummary(List<GovMandiPrice> records) {
        GovMandiHistorySummaryDTO s = new GovMandiHistorySummaryDTO();
        if (records.isEmpty()) return s;

        BigDecimal avgModal = calculateAverageModal(records);
        s.setAverageModalPrice(avgModal);
        s.setAverageModalPricePerKg(avgModal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        BigDecimal minPrice = records.stream().map(GovMandiPrice::getMinPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = records.stream().map(GovMandiPrice::getMaxPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        s.setMinPrice(minPrice);
        s.setMaxPrice(maxPrice);

        // Highest and lowest recorded modal prices with their market and date
        GovMandiPrice highestRec = records.stream()
                .filter(p -> p.getModalPrice() != null)
                .max(Comparator.comparing(GovMandiPrice::getModalPrice))
                .orElse(null);

        GovMandiPrice lowestRec = records.stream()
                .filter(p -> p.getModalPrice() != null)
                .min(Comparator.comparing(GovMandiPrice::getModalPrice))
                .orElse(null);

        if (highestRec != null) {
            s.setHighestPrice(highestRec.getModalPrice());
            s.setHighestPriceDate(highestRec.getArrivalDate());
            s.setHighestPriceMarket(highestRec.getMarket());
        }

        if (lowestRec != null) {
            s.setLowestPrice(lowestRec.getModalPrice());
            s.setLowestPriceDate(lowestRec.getArrivalDate());
            s.setLowestPriceMarket(lowestRec.getMarket());
        }

        s.setTotalArrivalRecords(records.size());
        int tradingDays = (int) records.stream().map(GovMandiPrice::getArrivalDate).distinct().count();
        s.setTotalTradingDays(tradingDays);

        // Trend calculation across chronological start to end
        if (records.size() >= 2) {
            List<GovMandiPrice> chrono = new ArrayList<>(records);
            chrono.sort(Comparator.comparing(GovMandiPrice::getArrivalDate));
            BigDecimal firstModal = chrono.get(0).getModalPrice();
            BigDecimal lastModal = chrono.get(chrono.size() - 1).getModalPrice();

            if (firstModal != null && firstModal.compareTo(BigDecimal.ZERO) > 0 && lastModal != null) {
                double diff = (lastModal.doubleValue() - firstModal.doubleValue()) / firstModal.doubleValue() * 100.0;
                double rounded = Math.round(diff * 10.0) / 10.0;
                s.setPriceChangePercentage(rounded);
                if (rounded > 1.0) {
                    s.setTrend("RISING");
                    s.setComparisonText("+" + rounded + "% from beginning to end of recorded period");
                } else if (rounded < -1.0) {
                    s.setTrend("FALLING");
                    s.setComparisonText(rounded + "% from beginning to end of recorded period");
                } else {
                    s.setTrend("STABLE");
                    s.setComparisonText("Stable (0.0% variance) across period");
                }
            } else {
                s.setTrend("STABLE");
                s.setPriceChangePercentage(0.0);
            }
        } else {
            s.setTrend("INSUFFICIENT_DATA");
            s.setPriceChangePercentage(0.0);
            s.setComparisonText("Single arrival observation recorded");
        }

        return s;
    }

    private List<YearlyPriceSummaryDTO> computeMultiYearComparison(String commodity, String state, String district, String market) {
        List<Integer> years = historyRepository.findAvailableYears(state, commodity);
        List<YearlyPriceSummaryDTO> list = new ArrayList<>();
        BigDecimal prevYearAvg = null;

        // Sort ascending to calculate year-over-year growth
        List<Integer> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears);

        for (int yr : sortedYears) {
            LocalDate start = LocalDate.of(yr, 1, 1);
            LocalDate end = LocalDate.of(yr, 12, 31);
            List<GovMandiPrice> yearRecs = historyRepository.findHistoricalPrices(state, district, market, commodity, start, end);
            if (!yearRecs.isEmpty()) {
                BigDecimal avgModal = calculateAverageModal(yearRecs);
                BigDecimal avgPerKg = avgModal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal minPrice = yearRecs.stream().map(GovMandiPrice::getMinPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = yearRecs.stream().map(GovMandiPrice::getMaxPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal highModal = yearRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal lowModal = yearRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                int tradingDays = (int) yearRecs.stream().map(GovMandiPrice::getArrivalDate).distinct().count();

                Double yoy = null;
                String trend = "STABLE";
                if (prevYearAvg != null && prevYearAvg.compareTo(BigDecimal.ZERO) > 0) {
                    double diff = (avgModal.doubleValue() - prevYearAvg.doubleValue()) / prevYearAvg.doubleValue() * 100.0;
                    yoy = Math.round(diff * 10.0) / 10.0;
                    if (yoy > 1.0) trend = "RISING";
                    else if (yoy < -1.0) trend = "FALLING";
                }

                list.add(new YearlyPriceSummaryDTO(yr, avgModal, avgPerKg, minPrice, maxPrice, highModal, lowModal, yearRecs.size(), tradingDays, yoy, trend));
                prevYearAvg = avgModal;
            }
        }

        // Return descending for display (most recent first)
        Collections.reverse(list);
        return list;
    }

    private List<YearlyPriceSummaryDTO> computeSameMonthAcrossYears(String commodity, String state, String district, String market, int month) {
        List<Integer> years = historyRepository.findAvailableYears(state, commodity);
        List<YearlyPriceSummaryDTO> list = new ArrayList<>();
        List<Integer> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears);
        BigDecimal prevMonthAvg = null;

        for (int yr : sortedYears) {
            YearMonth ym = YearMonth.of(yr, month);
            List<GovMandiPrice> monthRecs = historyRepository.findHistoricalPrices(state, district, market, commodity, ym.atDay(1), ym.atEndOfMonth());
            if (!monthRecs.isEmpty()) {
                BigDecimal avgModal = calculateAverageModal(monthRecs);
                BigDecimal avgPerKg = avgModal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal minPrice = monthRecs.stream().map(GovMandiPrice::getMinPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = monthRecs.stream().map(GovMandiPrice::getMaxPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal highModal = monthRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal lowModal = monthRecs.stream().map(GovMandiPrice::getModalPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                int tradingDays = (int) monthRecs.stream().map(GovMandiPrice::getArrivalDate).distinct().count();

                Double yoy = null;
                String trend = "STABLE";
                if (prevMonthAvg != null && prevMonthAvg.compareTo(BigDecimal.ZERO) > 0) {
                    double diff = (avgModal.doubleValue() - prevMonthAvg.doubleValue()) / prevMonthAvg.doubleValue() * 100.0;
                    yoy = Math.round(diff * 10.0) / 10.0;
                    if (yoy > 1.0) trend = "RISING";
                    else if (yoy < -1.0) trend = "FALLING";
                }

                list.add(new YearlyPriceSummaryDTO(yr, avgModal, avgPerKg, minPrice, maxPrice, highModal, lowModal, monthRecs.size(), tradingDays, yoy, trend));
                prevMonthAvg = avgModal;
            }
        }

        Collections.reverse(list);
        return list;
    }

    private BigDecimal calculateAverageModal(List<GovMandiPrice> recs) {
        if (recs == null || recs.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (GovMandiPrice p : recs) {
            if (p.getModalPrice() != null) {
                sum = sum.add(p.getModalPrice());
                count++;
            }
        }
        if (count == 0) return BigDecimal.ZERO;
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private List<Integer> getAvailableYears(String state, String commodity) {
        List<Integer> years = historyRepository.findAvailableYears(state, commodity);
        if (years == null || years.isEmpty()) {
            return Arrays.asList(LocalDate.now().getYear(), LocalDate.now().getYear() - 1);
        }
        return years;
    }

    private String cleanParam(String val, String fallback) {
        if (val == null || val.trim().isEmpty() || val.trim().equalsIgnoreCase("all")) {
            return fallback;
        }
        return val.trim();
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(raw.trim(), API_DATE_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw.trim());
            } catch (Exception ex) {
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
