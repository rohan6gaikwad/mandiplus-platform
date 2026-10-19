package com.rtech.mandiplus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtech.mandiplus.config.LiveFeedWebSocketHandler;
import com.rtech.mandiplus.entity.MandiPrice;
import com.rtech.mandiplus.repository.MandiPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@EnableScheduling
public class MandiRateService {

    @Autowired
    private MandiPriceRepository mandiPriceRepository;

    @Autowired
    private LiveFeedWebSocketHandler webSocketHandler;

    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 8000) // Simulates live market price fluctuations every 8 seconds
    public void simulateMandiFluctuations() {
        List<MandiPrice> prices = mandiPriceRepository.findAll();
        if (prices.isEmpty()) {
            return;
        }

        // Pick a random crop price index
        MandiPrice cropPrice = prices.get(random.nextInt(prices.size()));

        // Price fluctuation delta: absolute rupee change between -25.00 and +30.00
        double change = -25.0 + (random.nextDouble() * 55.0);

        cropPrice.fluctuate(change);

        mandiPriceRepository.save(cropPrice);

        // Publish live updates to all WebSocket clients
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "MANDI_UPDATE");
            message.put("id", cropPrice.getId());
            message.put("cropName", cropPrice.getCropName());
            message.put("variety", cropPrice.getVariety());
            message.put("marketName", cropPrice.getMarketName());
            message.put("pricePerQuintal", cropPrice.getPricePerQuintal());
            message.put("priceChangeYesterday", cropPrice.getPriceChangeYesterday());

            String payload = objectMapper.writeValueAsString(message);
            webSocketHandler.broadcast(payload);
        } catch (Exception e) {
            System.err.println("Failed to broadcast live Mandi WebSocket tick: " + e.getMessage());
        }
    }

    public void triggerManualRefresh() {
        List<MandiPrice> prices = mandiPriceRepository.findAll();
        if (prices.isEmpty()) {
            return;
        }
        for (MandiPrice price : prices) {
            double change = -25.0 + (random.nextDouble() * 55.0);
            price.fluctuate(change);
        }
        mandiPriceRepository.saveAll(prices);
    }

    public List<MandiPrice> getRatesForCrop(String cropName) {
        return mandiPriceRepository.findByCropNameContainingIgnoreCase(cropName);
    }

    public MandiPrice saveMandiRate(MandiPrice mandiPrice) {
        MandiPrice saved = mandiPriceRepository.save(mandiPrice);
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "MANDI_BENCHMARK");
            message.put("cropName", saved.getCropName());
            message.put("pricePerQuintal", saved.getPricePerQuintal());
            webSocketHandler.broadcast(objectMapper.writeValueAsString(message));
        } catch (Exception ignored) {}
        return saved;
    }
}
