package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUserEmail(String email);
    Optional<DeliveryPartner> findByUserId(Long userId);
    Optional<DeliveryPartner> findByPhone(String phone);
    boolean existsByPhone(String phone);
    long countByIsOnlineTrue();
}
