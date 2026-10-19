package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GovMandiPriceRepository extends JpaRepository<GovMandiPrice, Long> {

    Optional<GovMandiPrice> findByStateAndDistrictAndMarketAndCommodityAndVarietyAndGradeAndArrivalDate(
            String state, String district, String market, String commodity, String variety, String grade, LocalDate arrivalDate
    );

    @Query("SELECT DISTINCT p.district FROM GovMandiPrice p WHERE p.state = :state ORDER BY p.district ASC")
    List<String> findDistinctDistrictsByState(@Param("state") String state);

    @Query("SELECT DISTINCT p.market FROM GovMandiPrice p WHERE p.state = :state AND p.district = :district ORDER BY p.market ASC")
    List<String> findDistinctMarketsByStateAndDistrict(@Param("state") String state, @Param("district") String district);

    @Query("SELECT DISTINCT p.commodity FROM GovMandiPrice p WHERE p.state = :state ORDER BY p.commodity ASC")
    List<String> findDistinctCommoditiesByState(@Param("state") String state);

    @Query("SELECT p FROM GovMandiPrice p WHERE p.state = :state AND p.district = :district AND p.market = :market AND LOWER(p.commodity) = LOWER(:commodity) ORDER BY p.arrivalDate DESC")
    List<GovMandiPrice> findHistoryByMarketAndCommodity(
            @Param("state") String state,
            @Param("district") String district,
            @Param("market") String market,
            @Param("commodity") String commodity,
            Pageable pageable
    );

    @Query("SELECT p FROM GovMandiPrice p WHERE p.state = :state AND LOWER(p.commodity) = LOWER(:commodity) AND p.market <> :excludeMarket ORDER BY p.arrivalDate DESC")
    List<GovMandiPrice> findNearbyPricesByCommodity(
            @Param("state") String state,
            @Param("commodity") String commodity,
            @Param("excludeMarket") String excludeMarket,
            Pageable pageable
    );

    @Query("SELECT p FROM GovMandiPrice p WHERE p.state = :state AND p.district = :district AND LOWER(p.commodity) = LOWER(:commodity) AND p.market <> :excludeMarket ORDER BY p.arrivalDate DESC")
    List<GovMandiPrice> findDistrictPricesByCommodity(
            @Param("state") String state,
            @Param("district") String district,
            @Param("commodity") String commodity,
            @Param("excludeMarket") String excludeMarket,
            Pageable pageable
    );

    @Query("SELECT MAX(p.arrivalDate) FROM GovMandiPrice p WHERE p.state = :state")
    LocalDate findLatestArrivalDateByState(@Param("state") String state);

    @Query("SELECT COUNT(DISTINCT p.commodity) FROM GovMandiPrice p WHERE p.state = :state")
    Long countDistinctCommodities(@Param("state") String state);

    @Query("SELECT COUNT(DISTINCT p.market) FROM GovMandiPrice p WHERE p.state = :state")
    Long countDistinctMarkets(@Param("state") String state);

    List<GovMandiPrice> findTop20ByStateOrderByArrivalDateDesc(String state);
}
