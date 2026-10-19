package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(String phone);

    List<OtpChallenge> findByPhoneOrderByCreatedAtDesc(String phone);

    @Query("SELECT COUNT(o) FROM OtpChallenge o WHERE o.phone = :phone AND o.createdAt >= :since")
    long countRequestsSince(@Param("phone") String phone, @Param("since") LocalDateTime since);

    @Query("SELECT o FROM OtpChallenge o WHERE o.phone = :phone ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpChallenge> findLatestByPhone(@Param("phone") String phone);
}
