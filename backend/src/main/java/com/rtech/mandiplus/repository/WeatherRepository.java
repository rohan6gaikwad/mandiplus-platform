package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByLocationContainingIgnoreCase(String location);
}
