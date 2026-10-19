package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.AdminStatsDTO;
import com.rtech.mandiplus.dto.DeliveryTaskDTO;
import com.rtech.mandiplus.dto.ProductDTO;
import com.rtech.mandiplus.entity.Complaint;
import com.rtech.mandiplus.entity.DeliveryAuditLog;
import com.rtech.mandiplus.entity.MandiPrice;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.MandiPriceRepository;
import com.rtech.mandiplus.config.LiveFeedWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtech.mandiplus.service.AdminService;
import com.rtech.mandiplus.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private MandiPriceRepository mandiPriceRepository;

    @Autowired
    private LiveFeedWebSocketHandler webSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Get all Mandi Prices for Admin Panel
    @GetMapping("/mandi")
    public ResponseEntity<List<MandiPrice>> getAdminMandiRates() {
        return ResponseEntity.ok(mandiPriceRepository.findAll());
    }

    // Add new Mandi Price record
    @PostMapping("/mandi")
    public ResponseEntity<?> addMandiRate(@RequestBody MandiPrice newPrice) {
        try {
            if (newPrice.getPriceChangeYesterday() == null) {
                newPrice.setPriceChangeYesterday(java.math.BigDecimal.ZERO);
            }
            MandiPrice savedPrice = mandiPriceRepository.save(newPrice);
            broadcastMandiUpdate(savedPrice);
            return ResponseEntity.ok(savedPrice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Edit Mandi Price record
    @PutMapping("/mandi/{id}")
    public ResponseEntity<?> updateMandiRate(@PathVariable Long id, @RequestBody MandiPrice updatedFields) {
        try {
            MandiPrice cropPrice = mandiPriceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Error: Mandi Price record not found!"));
            
            cropPrice.setCropName(updatedFields.getCropName());
            cropPrice.setVariety(updatedFields.getVariety());
            cropPrice.setMarketName(updatedFields.getMarketName());
            cropPrice.setState(updatedFields.getState());
            cropPrice.setPricePerQuintal(updatedFields.getPricePerQuintal());
            if (updatedFields.getPriceChangeYesterday() != null) {
                cropPrice.setPriceChangeYesterday(updatedFields.getPriceChangeYesterday());
            }
            
            MandiPrice savedPrice = mandiPriceRepository.save(cropPrice);
            broadcastMandiUpdate(savedPrice);
            return ResponseEntity.ok(savedPrice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete Mandi Price record
    @DeleteMapping("/mandi/{id}")
    public ResponseEntity<?> deleteMandiRate(@PathVariable Long id) {
        try {
            mandiPriceRepository.deleteById(id);
            // Broadcast delete action or just update trigger
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "MANDI_DELETE");
            message.put("id", id);
            String payload = objectMapper.writeValueAsString(message);
            webSocketHandler.broadcast(payload);
            return ResponseEntity.ok("Mandi Price record deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void broadcastMandiUpdate(MandiPrice cropPrice) {
        try {
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "MANDI_UPDATE");
            message.put("id", cropPrice.getId());
            message.put("cropName", cropPrice.getCropName());
            message.put("variety", cropPrice.getVariety());
            message.put("marketName", cropPrice.getMarketName());
            message.put("pricePerQuintal", cropPrice.getPricePerQuintal());
            message.put("priceChangeYesterday", cropPrice.getPriceChangeYesterday());

            String payload = objectMapper.writeValueAsString(message);
            webSocketHandler.broadcast(payload);
        } catch (Exception e) {
            System.err.println("Failed to broadcast Admin Mandi WebSocket update: " + e.getMessage());
        }
    }

    // View global platform metrics
    @GetMapping("/metrics")
    public ResponseEntity<AdminStatsDTO> getGlobalMetrics() {
        return ResponseEntity.ok(adminService.getGlobalMetrics());
    }

    // View registered users list
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Toggle user status (Activate/Deactivate)
    @RequestMapping(value = {"/users/{id}/toggle", "/users/{id}/toggle-active"}, method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            User updatedUser = adminService.toggleUserStatus(id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // View system disputes/complaints list
    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(adminService.getAllComplaints());
    }

    // Update complaint resolution status
    @PutMapping({"/complaints/{id}/status", "/complaints/{id}/resolve"})
    public ResponseEntity<?> resolveComplaint(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String remarks) {
        try {
            Complaint resolvedComplaint = adminService.resolveComplaint(id, status);
            return ResponseEntity.ok(resolvedComplaint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Configure system parameters (escrow commission, logistics cover)
    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestParam double commission, @RequestParam double insurance) {
        try {
            adminService.updateSystemParameters(commission, insurance);
            return ResponseEntity.ok("System parameters updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Produce Moderation Desk: Get all crops (with optional status filter e.g. PENDING_APPROVAL, AVAILABLE, REJECTED)
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllAdminProducts(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getAllProducts(status));
    }

    // Produce Moderation Desk: Approve crop listing with optional price or photo adjustments
    @PutMapping({"/products/{id}/approve", "/products/{id}/status"})
    public ResponseEntity<?> approveProduct(
            @PathVariable Long id,
            @RequestBody(required = false) ProductDTO approvalData,
            @RequestParam(required = false) String status) {
        try {
            ProductDTO approved = adminService.approveProduct(id, approvalData);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Produce Moderation Desk: Reject crop listing with explanation feedback for farmer
    @PutMapping("/products/{id}/reject")
    public ResponseEntity<?> rejectProduct(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String reason) {
        try {
            String finalReason = reason;
            if (body != null && body.containsKey("reason") && !body.get("reason").isBlank()) {
                finalReason = body.get("reason");
            }
            ProductDTO rejected = adminService.rejectProduct(id, finalReason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Produce Moderation Desk: Delete crop listing
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteAdminProduct(@PathVariable Long id) {
        try {
            adminService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully from catalog"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // Delivery Logistics & Oversight Desk
    // ==========================================

    // View all delivery tasks with optional status filter
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryTaskDTO>> getAllDeliveries(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(deliveryService.getAllDeliveries(status));
    }

    // Identify unaccepted, pending, or overdue delivery assignments
    @GetMapping("/deliveries/unaccepted")
    public ResponseEntity<List<DeliveryTaskDTO>> getUnacceptedDeliveries() {
        return ResponseEntity.ok(deliveryService.getUnacceptedDeliveries());
    }

    // Reassign eligible delivery to a different delivery partner
    @PostMapping("/deliveries/{id}/reassign")
    public ResponseEntity<?> reassignDelivery(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long partnerId,
            Authentication authentication) {
        try {
            Long finalPartnerId = partnerId;
            if (finalPartnerId == null && body != null && body.get("partnerId") != null) {
                finalPartnerId = Long.valueOf(body.get("partnerId").toString());
            }
            if (finalPartnerId == null) {
                return ResponseEntity.badRequest().body("partnerId is required for reassignment.");
            }
            String adminEmail = authentication != null ? authentication.getName() : "admin@gmail.com";
            DeliveryTaskDTO reassigned = deliveryService.reassignDelivery(id, finalPartnerId, adminEmail);
            return ResponseEntity.ok(reassigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // View immutable audit trail for a specific delivery task
    @GetMapping("/deliveries/{id}/audit")
    public ResponseEntity<List<DeliveryAuditLog>> getDeliveryAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getAuditLogsForTask(id));
    }

    // View full system audit history of delivery transitions
    @GetMapping("/deliveries/audit")
    public ResponseEntity<List<DeliveryAuditLog>> getAllDeliveryAuditLogs() {
        return ResponseEntity.ok(deliveryService.getAllAuditLogs());
    }
}
