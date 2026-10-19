package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.DeliveryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAuditLogRepository extends JpaRepository<DeliveryAuditLog, Long> {
    List<DeliveryAuditLog> findByDeliveryTaskIdOrderByCreatedAtDesc(Long deliveryTaskId);
    List<DeliveryAuditLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<DeliveryAuditLog> findAllByOrderByCreatedAtDesc();
}
