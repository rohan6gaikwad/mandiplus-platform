package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GovernmentMandiHistoryRepository extends JpaRepository<GovMandiPrice, Long> {

    Optional<GovMandiPrice> findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
            String state, String district, String market, String commodity, String variety, String grade, LocalDate arrivalDate
    );

    @Query("SELECT p FROM GovMandiPrice p WHERE " +
           "(:state IS NULL OR :state = '' OR LOWER(p.state) = LOWER(:state)) AND " +
           "(:district IS NULL OR :district = '' OR LOWER(p.district) = LOWER(:district)) AND " +
           "(:market IS NULL OR :market = '' OR LOWER(p.market) = LOWER(:market)) AND " +
           "LOWER(p.commodity) = LOWER(:commodity) AND " +
           "p.arrivalDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.arrivalDate DESC, p.market ASC")
    List<GovMandiPrice> findHistoricalPrices(
            @Param("state") String state,
            @Param("district") String district,
            @Param("market") String market,
            @Param("commodity") String commodity,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT p FROM GovMandiPrice p WHERE " +
           "(:state IS NULL OR :state = '' OR LOWER(p.state) = LOWER(:state)) AND " +
           "(:district IS NULL OR :district = '' OR LOWER(p.district) = LOWER(:district)) AND " +
           "(:market IS NULL OR :market = '' OR LOWER(p.market) = LOWER(:market)) AND " +
           "LOWER(p.commodity) = LOWER(:commodity) AND " +
           "p.arrivalDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.arrivalDate DESC, p.market ASC")
    Page<GovMandiPrice> findHistoricalPricesPage(
            @Param("state") String state,
            @Param("district") String district,
            @Param("market") String market,
            @Param("commodity") String commodity,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT p FROM GovMandiPrice p WHERE " +
           "(:state IS NULL OR :state = '' OR LOWER(p.state) = LOWER(:state)) AND " +
           "(:district IS NULL OR :district = '' OR LOWER(p.district) = LOWER(:district)) AND " +
           "(:market IS NULL OR :market = '' OR LOWER(p.market) = LOWER(:market)) AND " +
           "LOWER(p.commodity) = LOWER(:commodity) AND " +
           "p.arrivalDate = :date " +
           "ORDER BY p.market ASC")
    List<GovMandiPrice> findDailyPrices(
            @Param("state") String state,
            @Param("district") String district,
            @Param("market") String market,
            @Param("commodity") String commodity,
            @Param("date") LocalDate date
    );

    @Query("SELECT DISTINCT YEAR(p.arrivalDate) FROM GovMandiPrice p WHERE " +
           "(:state IS NULL OR :state = '' OR LOWER(p.state) = LOWER(:state)) AND " +
           "(:commodity IS NULL OR :commodity = '' OR LOWER(p.commodity) = LOWER(:commodity)) " +
           "ORDER BY YEAR(p.arrivalDate) DESC")
    List<Integer> findAvailableYears(
            @Param("state") String state,
            @Param("commodity") String commodity
    );

    @Query("SELECT DISTINCT p.commodity FROM GovMandiPrice p ORDER BY p.commodity ASC")
    List<String> findDistinctCommodities();

    @Query("SELECT DISTINCT p.state FROM GovMandiPrice p ORDER BY p.state ASC")
    List<String> findDistinctStates();

    @Query("SELECT DISTINCT p.district FROM GovMandiPrice p WHERE (:state IS NULL OR :state = '' OR p.state = :state) ORDER BY p.district ASC")
    List<String> findDistinctDistricts(@Param("state") String state);

    @Query("SELECT DISTINCT p.market FROM GovMandiPrice p WHERE (:state IS NULL OR :state = '' OR p.state = :state) AND (:district IS NULL OR :district = '' OR p.district = :district) ORDER BY p.market ASC")
    List<String> findDistinctMarkets(@Param("state") String state, @Param("district") String district);

    @Query("SELECT MIN(p.arrivalDate) FROM GovMandiPrice p WHERE LOWER(p.commodity) = LOWER(:commodity)")
    LocalDate findMinDateByCommodity(@Param("commodity") String commodity);

    @Query("SELECT MAX(p.arrivalDate) FROM GovMandiPrice p WHERE LOWER(p.commodity) = LOWER(:commodity)")
    LocalDate findMaxDateByCommodity(@Param("commodity") String commodity);

    @Query("SELECT MAX(p.importedAt) FROM GovMandiPrice p")
    LocalDateTime findLatestImportedAt();
}
