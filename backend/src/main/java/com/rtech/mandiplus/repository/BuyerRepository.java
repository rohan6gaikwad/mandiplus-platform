package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    Optional<Buyer> findByUser(User user);
    Optional<Buyer> findByUserEmail(String email);
    Optional<Buyer> findByPhone(String phone);
}
