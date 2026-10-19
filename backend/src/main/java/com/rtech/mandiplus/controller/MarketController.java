package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.entity.MandiPrice;
import com.rtech.mandiplus.entity.Weather;
import com.rtech.mandiplus.repository.MandiPriceRepository;
import com.rtech.mandiplus.repository.WeatherRepository;
import com.rtech.mandiplus.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class MarketController {

    @Autowired
    private MandiPriceRepository mandiPriceRepository;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private com.rtech.mandiplus.service.MandiRateService mandiRateService;

    @GetMapping("/mandi-rates")
    public ResponseEntity<List<MandiPrice>> getMandiRates(@RequestParam(required = false) String crop) {
        if (crop != null && !crop.isEmpty()) {
            return ResponseEntity.ok(mandiPriceRepository.findByCropNameContainingIgnoreCase(crop));
        }
        return ResponseEntity.ok(mandiPriceRepository.findAll());
    }

    @PostMapping("/mandi-rates/refresh")
    public ResponseEntity<List<MandiPrice>> refreshMandiRates() {
        mandiRateService.triggerManualRefresh();
        return ResponseEntity.ok(mandiPriceRepository.findAll());
    }

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(@RequestParam(defaultValue = "Nashik, Maharashtra") String location) {
        Weather liveWeather = weatherService.getLiveWeather(location);
        if (liveWeather != null) {
            return ResponseEntity.ok(liveWeather);
        }
        return ResponseEntity.notFound().build();
    }
}
