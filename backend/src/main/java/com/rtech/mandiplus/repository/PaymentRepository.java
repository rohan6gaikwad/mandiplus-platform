package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    @Query("SELECT p FROM Payment p WHERE p.buyer.user.email = :email ORDER BY p.createdAt DESC")
    List<Payment> findByBuyerUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
