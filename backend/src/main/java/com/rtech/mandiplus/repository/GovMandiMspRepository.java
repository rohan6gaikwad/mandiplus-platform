package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiMsp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovMandiMspRepository extends JpaRepository<GovMandiMsp, Long> {

    Optional<GovMandiMsp> findByCommodityIgnoreCase(String commodity);

    @Query("SELECT m FROM GovMandiMsp m WHERE LOWER(:commodity) LIKE LOWER(CONCAT('%', m.commodity, '%')) OR LOWER(m.commodity) LIKE LOWER(CONCAT('%', :commodity, '%'))")
    List<GovMandiMsp> findFuzzyByCommodity(@Param("commodity") String commodity);

    List<GovMandiMsp> findAllByOrderByCommodityAsc();
}
