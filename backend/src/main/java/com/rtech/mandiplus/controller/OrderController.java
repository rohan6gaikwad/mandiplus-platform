package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.OrderDTO;
import com.rtech.mandiplus.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Buyer API - View order history
    @GetMapping("/buyer/orders")
    public ResponseEntity<List<OrderDTO>> getBuyerOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getBuyerOrders(authentication.getName()));
    }

    // Farmer API - View incoming orders
    @GetMapping("/farmer/orders")
    public ResponseEntity<List<OrderDTO>> getFarmerOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getFarmerOrders(authentication.getName()));
    }

    // Buyer API - Purchase produce
    @PostMapping("/buyer/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderDTO orderDTO, Authentication authentication) {
        try {
            OrderDTO placedOrder = orderService.placeOrder(orderDTO, authentication.getName());
            return ResponseEntity.ok(placedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Combined API - Update shipment status
    @PutMapping({"/orders/{id}/status", "/farmer/orders/{id}/status"})
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String estimatedDeliveryDate,
            Authentication authentication) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(id, status, estimatedDeliveryDate, authentication.getName());
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Buyer Cancel Order
    @PutMapping({"/orders/{id}/cancel", "/buyer/orders/{id}/cancel"})
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Authentication authentication) {
        try {
            OrderDTO cancelled = orderService.cancelOrder(id, authentication.getName());
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
