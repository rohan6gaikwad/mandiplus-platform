package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findFirstByPhone(String phone);
    default Optional<User> findByPhone(String phone) {
        return findFirstByPhone(phone);
    }
    Boolean existsByPhone(String phone);
    long countByRole(String role);
    java.util.List<User> findByRole(String role);
}
