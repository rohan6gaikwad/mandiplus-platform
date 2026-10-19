package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmer(Farmer farmer);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByFarmerUserEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByStatusOrderByCreatedAtDesc(String status);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByStatusOrderByCreatedAt(String status);

    long countByStatus(String status);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByCropNameContainingIgnoreCaseAndStatus(String cropName, String status);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByLocationContainingIgnoreCaseAndStatus(String location, String status);

    @EntityGraph(attributePaths = {"farmer"})
    List<Product> findByCropNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus(
            String cropName, String location, String status);
}
