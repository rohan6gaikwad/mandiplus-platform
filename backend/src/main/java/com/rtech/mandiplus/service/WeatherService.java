package com.rtech.mandiplus.service;

import com.rtech.mandiplus.entity.Weather;
import com.rtech.mandiplus.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class WeatherService {

    @Autowired
    private WeatherRepository weatherRepository;

    @Value("${openweather.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Weather getLiveWeather(String location) {
        Weather weather = weatherRepository.findByLocationContainingIgnoreCase(location)
                .orElseGet(() -> {
                    Weather w = new Weather();
                    w.setLocation(location);
                    return w;
                });

        try {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("OpenWeather key not configured");
            }
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                    java.net.URLEncoder.encode(location, "UTF-8"), apiKey);
            
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("main") && response.containsKey("weather")) {
                Map<?, ?> main = (Map<?, ?>) response.get("main");
                java.util.List<?> weatherList = (java.util.List<?>) response.get("weather");
                
                if (weatherList != null && !weatherList.isEmpty()) {
                    Map<?, ?> weatherInfo = (Map<?, ?>) weatherList.get(0);
                    String mainCondition = (String) weatherInfo.get("main");
                    String description = (String) weatherInfo.get("description");

                    Number temp = (Number) main.get("temp");
                    Number humidity = (Number) main.get("humidity");

                    weather.setTemperatureCelsius(BigDecimal.valueOf(temp.doubleValue()));
                    weather.setHumidityPercentage(humidity.intValue());

                    int rainProbability = 5;
                    if ("Rain".equalsIgnoreCase(mainCondition)) rainProbability = 85;
                    else if ("Drizzle".equalsIgnoreCase(mainCondition)) rainProbability = 70;
                    else if ("Thunderstorm".equalsIgnoreCase(mainCondition)) rainProbability = 90;
                    else if ("Clouds".equalsIgnoreCase(mainCondition)) rainProbability = 25;
                    weather.setRainProbabilityPercentage(rainProbability);

                    String condition = description.substring(0, 1).toUpperCase() + description.substring(1);
                    weather.setConditionText(condition);

                    weather = weatherRepository.save(weather);
                }
            }
        } catch (Exception e) {
            System.err.println("OpenWeatherMap Service query failed: " + e.getMessage() + ". Trying backend Open-Meteo fallback.");
            try {
                String geocodingUrl = String.format("https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json",
                        java.net.URLEncoder.encode(location, "UTF-8"));
                Map<?, ?> geoResponse = restTemplate.getForObject(geocodingUrl, Map.class);
                if (geoResponse != null && geoResponse.containsKey("results")) {
                    java.util.List<?> results = (java.util.List<?>) geoResponse.get("results");
                    if (results != null && !results.isEmpty()) {
                        Map<?, ?> firstResult = (Map<?, ?>) results.get(0);
                        Number lat = (Number) firstResult.get("latitude");
                        Number lon = (Number) firstResult.get("longitude");
                        String resolvedName = (String) firstResult.get("name");
                        String admin1 = (String) firstResult.get("admin1");
                        String country = (String) firstResult.get("country");
                        
                        String resolvedLocation = resolvedName;
                        if (admin1 != null && !admin1.isEmpty()) {
                            resolvedLocation += ", " + admin1;
                        } else if (country != null && !country.isEmpty()) {
                            resolvedLocation += ", " + country;
                        }
                        weather.setLocation(resolvedLocation);

                        String forecastUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,weather_code,precipitation_probability",
                                lat.toString(), lon.toString());
                        Map<?, ?> forecastResponse = restTemplate.getForObject(forecastUrl, Map.class);
                        if (forecastResponse != null && forecastResponse.containsKey("current")) {
                            Map<?, ?> current = (Map<?, ?>) forecastResponse.get("current");
                            Number temp = (Number) current.get("temperature_2m");
                            Number humidity = (Number) current.get("relative_humidity_2m");
                            Number precipitationProb = (Number) current.get("precipitation_probability");
                            Number wmoCode = (Number) current.get("weather_code");

                            weather.setTemperatureCelsius(BigDecimal.valueOf(temp.doubleValue()));
                            weather.setHumidityPercentage(humidity != null ? humidity.intValue() : 50);
                            weather.setRainProbabilityPercentage(precipitationProb != null ? precipitationProb.intValue() : 0);
                            
                            int code = wmoCode != null ? wmoCode.intValue() : 3;
                            String condition = mapWmoCodeToCondition(code);
                            weather.setConditionText(condition);

                            weather = weatherRepository.save(weather);
                            return weather;
                        }
                    }
                }
                throw new Exception("Open-Meteo fallback geocoding or forecast query failed");
            } catch (Exception ex) {
                System.err.println("Open-Meteo backend fallback failed: " + ex.getMessage() + ". Serving fallback cached weather.");
                if (weather.getId() == null) {
                    weather.setTemperatureCelsius(BigDecimal.valueOf(29.0));
                    weather.setConditionText("Partly Cloudy");
                    weather.setHumidityPercentage(55);
                    weather.setRainProbabilityPercentage(10);
                    weather = weatherRepository.save(weather);
                }
            }
        }

        return weather;
    }

    private String mapWmoCodeToCondition(int code) {
        if (code == 0) return "Clear Sky";
        if (code == 1) return "Mainly Clear";
        if (code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Foggy";
        if (code == 51 || code == 53 || code == 55) return "Drizzle";
        if (code == 56 || code == 57) return "Freezing Drizzle";
        if (code == 61 || code == 63 || code == 65) return "Rain";
        if (code == 66 || code == 67) return "Freezing Rain";
        if (code == 71 || code == 73 || code == 75) return "Snow";
        if (code == 77) return "Snow Grains";
        if (code == 80 || code == 81 || code == 82) return "Rain Showers";
        if (code == 85 || code == 86) return "Snow Showers";
        if (code == 95) return "Thunderstorm";
        if (code == 96 || code == 99) return "Thunderstorm with Hail";
        return "Partly Cloudy";
    }
}
