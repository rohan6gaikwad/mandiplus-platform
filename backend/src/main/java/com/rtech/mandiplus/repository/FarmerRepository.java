package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByUser(User user);
    Optional<Farmer> findByUserEmail(String email);
    Optional<Farmer> findByPhone(String phone);
}
