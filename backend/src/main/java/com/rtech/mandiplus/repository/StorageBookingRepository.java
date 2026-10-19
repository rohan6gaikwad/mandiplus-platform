package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.StorageBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageBookingRepository extends JpaRepository<StorageBooking, Long> {
    @EntityGraph(attributePaths = {"farmer", "buyer", "coldStorage"})
    List<StorageBooking> findByFarmerUserEmail(String email);

    @EntityGraph(attributePaths = {"farmer", "buyer", "coldStorage"})
    List<StorageBooking> findByBuyerUserEmail(String email);
    
    @EntityGraph(attributePaths = {"farmer", "buyer", "coldStorage"})
    @Query("SELECT sb FROM StorageBooking sb WHERE sb.coldStorage.manager.email = :email")
    List<StorageBooking> findByColdStorageManagerEmail(@Param("email") String email);

    @Query("SELECT COALESCE(SUM(sb.quantityTons), 0) FROM StorageBooking sb WHERE sb.coldStorage.id = :storageId AND sb.status IN ('APPROVED', 'ACTIVE')")
    java.math.BigDecimal sumOccupiedCapacityByStorageId(@Param("storageId") Long storageId);

    @Query("SELECT COALESCE(SUM(sb.quantityTons), 0) FROM StorageBooking sb WHERE sb.coldStorage.id = :storageId AND sb.status IN ('APPROVED', 'ACTIVE')")
    java.math.BigDecimal sumOccupiedCapacity(@Param("storageId") Long storageId);
}
