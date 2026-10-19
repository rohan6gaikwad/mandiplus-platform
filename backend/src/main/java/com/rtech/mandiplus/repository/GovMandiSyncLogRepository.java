package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovMandiSyncLogRepository extends JpaRepository<GovMandiSyncLog, Long> {

    List<GovMandiSyncLog> findTop15ByOrderBySyncTimestampDesc();

    Optional<GovMandiSyncLog> findTopByStatusOrderBySyncTimestampDesc(String status);
}
