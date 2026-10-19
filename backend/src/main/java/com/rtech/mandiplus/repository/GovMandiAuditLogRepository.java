package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.GovMandiAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovMandiAuditLogRepository extends JpaRepository<GovMandiAuditLog, Long> {

    List<GovMandiAuditLog> findTop25ByOrderByTimestampDesc();
}
