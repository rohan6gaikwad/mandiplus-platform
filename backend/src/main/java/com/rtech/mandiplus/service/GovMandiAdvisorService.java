package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.GovMandiAdvisory;
import com.rtech.mandiplus.entity.GovMandiMsp;
import com.rtech.mandiplus.entity.GovMandiPrice;
import com.rtech.mandiplus.repository.GovMandiAdvisoryRepository;
import com.rtech.mandiplus.repository.GovMandiMspRepository;
import com.rtech.mandiplus.repository.GovMandiPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GovMandiAdvisorService {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Autowired
    private GovMandiPriceRepository priceRepository;

    @Autowired
    private GovMandiMspRepository mspRepository;

    @Autowired
    private GovMandiAdvisoryRepository advisoryRepository;

    /**
     * Get dynamic dropdown filter options (Districts, Markets per district, Commodities).
     */
    public GovMandiFilterOptionsDTO getFilterOptions(String state) {
        String targetState = (state == null || state.trim().isEmpty()) ? "Maharashtra" : state.trim();

        List<String> districts = priceRepository.findDistinctDistrictsByState(targetState);
        List<String> commodities = priceRepository.findDistinctCommoditiesByState(targetState);

        Map<String, List<String>> districtMarkets = new HashMap<>();
        for (String dist : districts) {
            List<String> markets = priceRepository.findDistinctMarketsByStateAndDistrict(targetState, dist);
            districtMarkets.put(dist, markets);
        }

        // If database has no synced records yet, provide standard Maharashtra reference defaults
        if (districts.isEmpty()) {
            districts = Arrays.asList("Nashik", "Ahilyanagar", "Pune", "Kolhapur", "Nagpur", "Solapur", "Chhatrapati Sambhajinagar", "Jalgaon");
            commodities = Arrays.asList("Tomato", "Onion", "Maize", "Wheat", "Soyabean", "Cotton", "Potato", "Chilly Capsicum", "Gram");
            for (String dist : districts) {
                districtMarkets.put(dist, Collections.singletonList("APMC " + dist + " Yard"));
            }
        }

        return new GovMandiFilterOptionsDTO(targetState, districts, commodities, districtMarkets);
    }

    /**
     * Core price advisor engine for farmers and admins.
     */
    public GovMandiAdviceDTO getMandiAdvice(String state, String district, String market, String commodity) {
        String targetState = (state == null || state.trim().isEmpty()) ? "Maharashtra" : state.trim();
        String targetCommodity = commodity != null ? commodity.trim() : "Onion";
        String targetDistrict = district != null ? district.trim() : "Nashik";
        String targetMarket = market != null ? market.trim() : "";

        GovMandiAdviceDTO advice = new GovMandiAdviceDTO();
        advice.setState(targetState);
        advice.setDistrict(targetDistrict);
        advice.setMarket(targetMarket);
        advice.setCommodity(targetCommodity);
        advice.setSource("data.gov.in (Official Agmarknet Daily Feed)");

        // 1. Fetch latest prices and historical observations
        List<GovMandiPrice> history = new ArrayList<>();
        if (!targetMarket.isEmpty()) {
            history = priceRepository.findHistoryByMarketAndCommodity(
                    targetState, targetDistrict, targetMarket, targetCommodity, PageRequest.of(0, 10)
            );
        }

        // Fallback 1: If targetDistrict provided but no records for specific market, fetch district records
        if (history.isEmpty() && !targetDistrict.isEmpty()) {
            history = priceRepository.findDistrictPricesByCommodity(
                    targetState, targetDistrict, targetCommodity, "", PageRequest.of(0, 10)
            );
            if (!history.isEmpty()) {
                targetMarket = history.get(0).getMarket();
                advice.setMarket(targetMarket);
            }
        }

        // Fallback 2: If still empty (e.g. All Districts selected, or no arrivals in selected district), fetch state-wide records
        if (history.isEmpty()) {
            history = priceRepository.findNearbyPricesByCommodity(
                    targetState, targetCommodity, "", PageRequest.of(0, 10)
            );
            if (!history.isEmpty()) {
                targetMarket = history.get(0).getMarket();
                targetDistrict = history.get(0).getDistrict();
                advice.setMarket(targetMarket);
                advice.setDistrict(targetDistrict);
            }
        }

        if (history.isEmpty()) {
            // No data available for this market
            advice.setHasSuggestedRange(false);
            advice.setTrend("NOT_ENOUGH_DATA");
            advice.setIsStale(true);
            advice.setStaleWarning("No official arrival records have been reported for " + targetCommodity +
                    " in " + (targetMarket.isEmpty() ? targetDistrict : targetMarket) + " recently.");
            advice.setSuggestionExplanation("Official mandi data has not been received for this commodity selection. Please select another crop or district.");

            // Still attach MSP and advisories
            attachMspData(advice, targetCommodity);
            attachAdvisories(advice, targetCommodity, targetDistrict);
            return advice;
        }

        GovMandiPrice latest = history.get(0);
        advice.setVariety(latest.getVariety());
        advice.setGrade(latest.getGrade());
        advice.setArrivalDate(latest.getArrivalDate());
        advice.setReportingDateFormatted(latest.getArrivalDate().format(DISPLAY_DATE_FORMAT));

        // Official reported prices (₹/quintal)
        advice.setMinPrice(latest.getMinPrice());
        advice.setMaxPrice(latest.getMaxPrice());
        advice.setModalPrice(latest.getModalPrice());

        // Price per kg (1 Quintal = 100 kg)
        BigDecimal pricePerKg = latest.getModalPrice().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        advice.setPricePerKg(pricePerKg);

        // 2. Data Freshness Analysis
        long daysOld = ChronoUnit.DAYS.between(latest.getArrivalDate(), LocalDate.now());
        if (daysOld > 3) {
            advice.setIsStale(true);
            advice.setStaleWarning("⚠️ Stale Data Warning: The latest arrival reported for this APMC was on " +
                    latest.getArrivalDate().format(DISPLAY_DATE_FORMAT) + " (" + daysOld + " days ago). Spot rates may differ as daily mandi arrivals fluctuate.");
        } else {
            advice.setIsStale(false);
            advice.setStaleWarning(null);
        }

        // 3. Reliable Forecast / Moving Average Baseline
        int dataPoints = history.size();
        advice.setHistoricalDataPoints(dataPoints);

        if (dataPoints >= 2) {
            BigDecimal sumModal = BigDecimal.ZERO;
            for (GovMandiPrice p : history) {
                sumModal = sumModal.add(p.getModalPrice());
            }
            BigDecimal movingAverage = sumModal.divide(BigDecimal.valueOf(dataPoints), 2, RoundingMode.HALF_UP);
            advice.setMovingAveragePrice(movingAverage);

            // Calculate trend against baseline
            double ratio = latest.getModalPrice().doubleValue() / (movingAverage.doubleValue() > 0 ? movingAverage.doubleValue() : 1.0);
            if (ratio > 1.02) {
                advice.setTrend("RISING");
                advice.setTrendPercentage(Math.round((ratio - 1.0) * 1000.0) / 10.0);
            } else if (ratio < 0.98) {
                advice.setTrend("FALLING");
                advice.setTrendPercentage(Math.round((1.0 - ratio) * 1000.0) / 10.0);
            } else {
                advice.setTrend("STABLE");
                advice.setTrendPercentage(0.0);
            }

            // Recommended selling price range based on moving average and reported boundaries
            BigDecimal suggestedMin = movingAverage.multiply(BigDecimal.valueOf(0.96)).max(latest.getMinPrice()).setScale(0, RoundingMode.HALF_UP);
            BigDecimal suggestedMax = movingAverage.multiply(BigDecimal.valueOf(1.05)).min(latest.getMaxPrice().multiply(BigDecimal.valueOf(1.02))).setScale(0, RoundingMode.HALF_UP);

            if (suggestedMax.compareTo(suggestedMin) < 0) {
                suggestedMax = suggestedMin.multiply(BigDecimal.valueOf(1.08)).setScale(0, RoundingMode.HALF_UP);
            }

            advice.setSuggestedMinPrice(suggestedMin);
            advice.setSuggestedMaxPrice(suggestedMax);
            advice.setSuggestedMinPricePerKg(suggestedMin.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            advice.setSuggestedMaxPricePerKg(suggestedMax.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            advice.setHasSuggestedRange(true);
            advice.setSuggestionExplanation("Derived from a " + dataPoints + "-arrival moving average baseline (₹" +
                    movingAverage.setScale(0, RoundingMode.HALF_UP) + "/qtl) from official APMC reports. Spot offers may vary based on crop moisture and grade.");
        } else {
            advice.setTrend("NOT_ENOUGH_DATA");
            advice.setTrendPercentage(0.0);
            advice.setHasSuggestedRange(false);
            advice.setSuggestionExplanation("Insufficient historical arrivals in this specific yard to calculate a moving average forecast. Please rely on the official reported modal price.");
        }

        // 4. Attach Verified Official Government MSP
        attachMspData(advice, targetCommodity);

        // 5. Compare Nearby Mandis
        advice.setNearbyMarkets(findNearbyComparisons(targetState, targetDistrict, targetMarket, targetCommodity, latest.getModalPrice()));

        // 6. Attach Farmer Advisories
        attachAdvisories(advice, targetCommodity, targetDistrict);

        return advice;
    }

    private void attachMspData(GovMandiAdviceDTO advice, String commodity) {
        Optional<GovMandiMsp> exact = mspRepository.findByCommodityIgnoreCase(commodity);
        if (exact.isPresent()) {
            GovMandiMsp msp = exact.get();
            advice.setMspPrice(msp.getMspPrice());
            advice.setIsMspVerified(msp.getVerified());
            advice.setMspSeason(msp.getEffectiveSeason());
            advice.setMspNotes(msp.getNotes() != null ? msp.getNotes() : "Official MSP fixed by Government of India (" + msp.getEffectiveSeason() + ")");
            return;
        }

        // Try fuzzy match
        List<GovMandiMsp> fuzzy = mspRepository.findFuzzyByCommodity(commodity);
        if (!fuzzy.isEmpty()) {
            GovMandiMsp msp = fuzzy.get(0);
            advice.setMspPrice(msp.getMspPrice());
            advice.setIsMspVerified(msp.getVerified());
            advice.setMspSeason(msp.getEffectiveSeason());
            advice.setMspNotes("Official MSP for " + msp.getCommodity() + " (" + msp.getEffectiveSeason() + ")");
        } else {
            advice.setIsMspVerified(false);
            advice.setMspPrice(null);
            advice.setMspSeason(null);
            advice.setMspNotes("No official Government MSP applicable for " + commodity + " (Horticulture / perishable produce is traded at open market auction prices).");
        }
    }

    private List<NearbyMarketPriceDTO> findNearbyComparisons(String state, String district, String market, String commodity, BigDecimal currentModal) {
        List<NearbyMarketPriceDTO> list = new ArrayList<>();
        Set<String> seenMarkets = new HashSet<>();
        if (market != null && !market.trim().isEmpty()) {
            seenMarkets.add(market);
        }

        // First find in same district if district is specified
        if (district != null && !district.trim().isEmpty()) {
            List<GovMandiPrice> districtPrices = priceRepository.findDistrictPricesByCommodity(state, district, commodity, market != null ? market : "", PageRequest.of(0, 5));
            for (GovMandiPrice p : districtPrices) {
                if (seenMarkets.add(p.getMarket())) {
                    BigDecimal diff = currentModal != null ? p.getModalPrice().subtract(currentModal) : BigDecimal.ZERO;
                    BigDecimal perKg = p.getModalPrice().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    list.add(new NearbyMarketPriceDTO(
                            p.getMarket(), p.getDistrict(), p.getModalPrice(), perKg, diff, p.getArrivalDate(), "Same District (" + p.getDistrict() + ")"
                    ));
                }
            }
        }

        // Add regional Maharashtra markets for this commodity
        List<GovMandiPrice> statePrices = priceRepository.findNearbyPricesByCommodity(state, commodity, market != null ? market : "", PageRequest.of(0, 10));
        for (GovMandiPrice p : statePrices) {
            if (seenMarkets.add(p.getMarket()) && list.size() < 6) {
                BigDecimal diff = currentModal != null ? p.getModalPrice().subtract(currentModal) : BigDecimal.ZERO;
                BigDecimal perKg = p.getModalPrice().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                list.add(new NearbyMarketPriceDTO(
                        p.getMarket(), p.getDistrict(), p.getModalPrice(), perKg, diff, p.getArrivalDate(), "Regional Hub (" + p.getDistrict() + ")"
                ));
            }
        }

        return list;
    }

    private void attachAdvisories(GovMandiAdviceDTO advice, String commodity, String district) {
        List<GovMandiAdvisory> advisories = advisoryRepository.findActiveForCommodityAndDistrict(commodity, district);
        List<GovMandiAdvisoryDTO> dtos = advisories.stream().map(a -> new GovMandiAdvisoryDTO(
                a.getId(), a.getCommodity(), a.getDistrict(), a.getTitle(), a.getAdvisoryText(), a.getAlertLevel(), a.getActive(), a.getCreatedAt(), a.getCreatedBy()
        )).collect(Collectors.toList());
        advice.setAdvisories(dtos);
    }
}
