package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserEmail(String email);
    List<Complaint> findByStatus(String status);
}
