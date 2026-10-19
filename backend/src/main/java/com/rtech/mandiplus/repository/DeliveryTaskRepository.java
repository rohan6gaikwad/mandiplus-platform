package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.DeliveryTask;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {

    List<DeliveryTask> findByStatus(String status);

    List<DeliveryTask> findByStatusIn(List<String> statuses);

    List<DeliveryTask> findByDeliveryPartnerUserEmailOrderByCreatedAtDesc(String email);

    List<DeliveryTask> findByDeliveryPartnerId(Long partnerId);

    Optional<DeliveryTask> findByTaskCode(String taskCode);

    @Query("SELECT t FROM DeliveryTask t WHERE t.order.id = :orderId")
    Optional<DeliveryTask> findByOrderId(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM DeliveryTask t WHERE t.id = :id")
    Optional<DeliveryTask> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT t FROM DeliveryTask t WHERE t.status IN ('AVAILABLE', 'DELIVERY_PENDING', 'DELIVERY_ASSIGNED', 'ASSIGNED') ORDER BY t.createdAt ASC")
    List<DeliveryTask> findUnacceptedAssignments();

    List<DeliveryTask> findAllByOrderByCreatedAtDesc();
}
