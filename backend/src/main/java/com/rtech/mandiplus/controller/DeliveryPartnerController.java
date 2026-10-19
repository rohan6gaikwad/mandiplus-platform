package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.DeliveryApplyRequest;
import com.rtech.mandiplus.dto.DeliveryLocationUpdateRequest;
import com.rtech.mandiplus.dto.DeliveryPartnerDTO;
import com.rtech.mandiplus.dto.DeliveryTaskDTO;
import com.rtech.mandiplus.dto.DeliveryTrackingDTO;
import com.rtech.mandiplus.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeliveryPartnerController {

    @Autowired
    private DeliveryService deliveryService;

    // Public Application Endpoint - Apply as a Delivery Partner (like Blinkit onboarding)
    @PostMapping("/public/delivery/apply")
    public ResponseEntity<?> applyForPartner(@Valid @RequestBody DeliveryApplyRequest request) {
        try {
            DeliveryPartnerDTO partner = deliveryService.applyForDeliveryPartner(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Application submitted successfully! Your account is activated.",
                    "partner", partner,
                    "trackingId", "MND-FLEET-" + partner.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Application failed: " + e.getMessage());
        }
    }

    // Authenticated Partner Profile
    @GetMapping("/delivery/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            DeliveryPartnerDTO partner = deliveryService.getPartnerProfile(authentication.getName());
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update Partner Profile (Vehicle, UPI, Emergency Contact)
    @PutMapping("/delivery/profile")
    public ResponseEntity<?> updateProfile(@RequestBody DeliveryPartnerDTO dto, Authentication authentication) {
        try {
            DeliveryPartnerDTO updated = deliveryService.updatePartnerProfile(authentication.getName(), dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Online / Offline Duty Status Toggle
    @PutMapping("/delivery/toggle-duty")
    public ResponseEntity<?> toggleDuty(Authentication authentication) {
        try {
            DeliveryPartnerDTO partner = deliveryService.toggleDuty(authentication.getName());
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Available Dispatch Tasks in the Hub
    @GetMapping("/delivery/available-tasks")
    public ResponseEntity<List<DeliveryTaskDTO>> getAvailableTasks(Authentication authentication) {
        return ResponseEntity.ok(deliveryService.getAvailableTasks(authentication.getName()));
    }

    // Partner's active & completed tasks
    @GetMapping("/delivery/my-tasks")
    public ResponseEntity<List<DeliveryTaskDTO>> getMyTasks(Authentication authentication) {
        return ResponseEntity.ok(deliveryService.getMyTasks(authentication.getName()));
    }

    // Accept an incoming delivery task
    @PostMapping("/delivery/tasks/{taskId}/accept")
    public ResponseEntity<?> acceptTask(
            @PathVariable Long taskId,
            @RequestParam(required = false) Boolean confirm,
            Authentication authentication) {
        try {
            if (Boolean.TRUE.equals(confirm)) {
                DeliveryTaskDTO task = deliveryService.acceptDelivery(taskId, authentication.getName());
                return ResponseEntity.ok(task);
            }
            DeliveryTaskDTO task = deliveryService.acceptTask(taskId, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Explicit partner acceptance (concurrency safe)
    @PostMapping("/delivery/tasks/{taskId}/accept-delivery")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long taskId, Authentication authentication) {
        try {
            DeliveryTaskDTO task = deliveryService.acceptDelivery(taskId, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Partner rejects an offered or assigned delivery request
    @PostMapping("/delivery/tasks/{taskId}/reject")
    public ResponseEntity<?> rejectDelivery(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        try {
            String reason = body != null ? body.get("reason") : null;
            DeliveryTaskDTO task = deliveryService.rejectDelivery(taskId, authentication.getName(), reason);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Partner confirms pickup of goods from farmgate
    @PostMapping("/delivery/tasks/{taskId}/confirm-pickup")
    public ResponseEntity<?> confirmPickup(@PathVariable Long taskId, Authentication authentication) {
        try {
            DeliveryTaskDTO task = deliveryService.confirmPickup(taskId, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Partner commences delivery transit towards destination
    @PostMapping("/delivery/tasks/{taskId}/start-transit")
    public ResponseEntity<?> startTransit(@PathVariable Long taskId, Authentication authentication) {
        try {
            DeliveryTaskDTO task = deliveryService.startTransit(taskId, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Partner submits real GPS coordinates from browser/mobile geolocation
    @PostMapping("/delivery/tasks/{taskId}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long taskId,
            @RequestBody DeliveryLocationUpdateRequest locationReq,
            Authentication authentication) {
        try {
            DeliveryTrackingDTO tracking = deliveryService.updatePartnerLocation(taskId, locationReq, authentication.getName());
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Track delivery by Order ID (Accessible to Buyer, Partner, Admin, Farmer)
    @GetMapping({"/delivery/track/{orderId}", "/buyer/orders/{orderId}/tracking"})
    public ResponseEntity<?> trackOrderByOrderId(@PathVariable Long orderId, Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(orderId, email, true);
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Track delivery by Task ID
    @GetMapping("/delivery/tasks/{taskId}/tracking")
    public ResponseEntity<?> trackTaskByTaskId(@PathVariable Long taskId, Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(taskId, email, false);
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Advance status (REACHED_PICKUP, PICKED_UP, IN_TRANSIT, DELIVERED with OTP)
    @PutMapping("/delivery/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status,
            @RequestParam(required = false) String otp,
            Authentication authentication) {
        try {
            DeliveryTaskDTO task = deliveryService.updateTaskStatus(taskId, status, otp, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Instant UPI Payout Withdrawal
    @PostMapping("/delivery/withdraw-payout")
    public ResponseEntity<?> withdrawPayout(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            String upiId = payload.get("upiId") != null ? payload.get("upiId").toString() : null;
            String msg = deliveryService.withdrawPayout(amount, upiId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
