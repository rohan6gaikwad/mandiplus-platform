package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiAdvisory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovMandiAdvisoryRepository extends JpaRepository<GovMandiAdvisory, Long> {

    List<GovMandiAdvisory> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT a FROM GovMandiAdvisory a WHERE a.active = true AND (LOWER(a.commodity) = LOWER(:commodity) OR a.commodity = 'ALL') AND (a.district IS NULL OR LOWER(a.district) = LOWER(:district)) ORDER BY a.createdAt DESC")
    List<GovMandiAdvisory> findActiveForCommodityAndDistrict(@Param("commodity") String commodity, @Param("district") String district);

    List<GovMandiAdvisory> findAllByOrderByCreatedAtDesc();
}
