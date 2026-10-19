package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather")
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "temperature_celsius", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "condition_text", nullable = false, length = 100)
    private String conditionText;

    @Column(name = "humidity_percentage", nullable = false)
    private Integer humidityPercentage;

    @Column(name = "rain_probability_percentage", nullable = false)
    private Integer rainProbabilityPercentage;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Weather() {
    }

    public Weather(Long id, String location, BigDecimal temperatureCelsius, String conditionText,
                   Integer humidityPercentage, Integer rainProbabilityPercentage, LocalDateTime updatedAt) {
        this.id = id;
        this.location = location;
        this.temperatureCelsius = temperatureCelsius;
        this.conditionText = conditionText;
        this.humidityPercentage = humidityPercentage;
        this.rainProbabilityPercentage = rainProbabilityPercentage;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(BigDecimal temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public Integer getHumidityPercentage() {
        return humidityPercentage;
    }

    public void setHumidityPercentage(Integer humidityPercentage) {
        this.humidityPercentage = humidityPercentage;
    }

    public Integer getRainProbabilityPercentage() {
        return rainProbabilityPercentage;
    }

    public void setRainProbabilityPercentage(Integer rainProbabilityPercentage) {
        this.rainProbabilityPercentage = rainProbabilityPercentage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
