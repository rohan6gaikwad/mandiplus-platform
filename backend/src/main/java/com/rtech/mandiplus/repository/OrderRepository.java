package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(Buyer buyer);

    @EntityGraph(attributePaths = {"buyer", "product", "product.farmer"})
    List<Order> findByBuyerUserEmail(String email);
    
    @EntityGraph(attributePaths = {"buyer", "product", "product.farmer"})
    @Query("SELECT o FROM Order o WHERE o.product.farmer.id = :farmerId")
    List<Order> findByFarmerId(@Param("farmerId") Long farmerId);
    
    @EntityGraph(attributePaths = {"buyer", "product", "product.farmer"})
    @Query("SELECT o FROM Order o WHERE o.product.farmer.user.email = :email")
    List<Order> findByFarmerUserEmail(@Param("email") String email);
}
