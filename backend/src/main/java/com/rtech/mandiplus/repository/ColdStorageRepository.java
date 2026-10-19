package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.ColdStorage;
import com.rtech.mandiplus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColdStorageRepository extends JpaRepository<ColdStorage, Long> {
    Optional<ColdStorage> findByManager(User manager);
    Optional<ColdStorage> findByManagerEmail(String email);
    List<ColdStorage> findByLocationContainingIgnoreCase(String location);
}
