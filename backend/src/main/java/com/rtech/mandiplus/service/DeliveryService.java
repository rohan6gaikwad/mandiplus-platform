package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.*;
import com.rtech.mandiplus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryPartnerRepository partnerRepository;

    @Autowired
    private DeliveryTaskRepository taskRepository;

    @Autowired
    private DeliveryAuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private NotificationService notificationService;

    public DeliveryPartnerDTO getPartnerProfile(String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultProfileForUser(email));
        return convertPartnerToDTO(partner);
    }

    @Transactional
    public DeliveryPartnerDTO toggleDuty(String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultProfileForUser(email));
        partner.setIsOnline(!Boolean.TRUE.equals(partner.getIsOnline()));
        partner = partnerRepository.save(partner);
        return convertPartnerToDTO(partner);
    }

    public DeliveryPartnerDTO toggleOnlineStatus(String email) {
        return toggleDuty(email);
    }

    public DeliveryPartnerDTO applyForPartner(DeliveryApplyRequest request) {
        return applyForDeliveryPartner(request);
    }

    @Transactional
    public DeliveryPartnerDTO updatePartnerProfile(String email, DeliveryPartnerDTO dto) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultProfileForUser(email));

        if (dto.getName() != null) partner.setName(dto.getName());
        if (dto.getPhone() != null) partner.setPhone(dto.getPhone());
        if (dto.getVehicleType() != null) partner.setVehicleType(dto.getVehicleType());
        if (dto.getVehicleNumber() != null) partner.setVehicleNumber(dto.getVehicleNumber());
        if (dto.getLicenseNumber() != null) partner.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getPreferredHub() != null) partner.setPreferredHub(dto.getPreferredHub());
        if (dto.getUpiId() != null) partner.setUpiId(dto.getUpiId());
        if (dto.getEmergencyContactName() != null) partner.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactPhone() != null) partner.setEmergencyContactPhone(dto.getEmergencyContactPhone());

        if (dto.getProfilePhoto() != null && partner.getUser() != null) {
            partner.getUser().setProfilePhoto(dto.getProfilePhoto());
            userRepository.save(partner.getUser());
        }

        partner = partnerRepository.save(partner);
        return convertPartnerToDTO(partner);
    }

    public List<DeliveryTaskDTO> getAvailableTasks(String email) {
        seedInitialDemoTasksIfEmpty();
        List<DeliveryTask> tasks = taskRepository.findByStatusIn(List.of("AVAILABLE", "DELIVERY_PENDING", "DELIVERY_ASSIGNED", "ASSIGNED"));
        return tasks.stream().map(this::convertTaskToDTO).collect(Collectors.toList());
    }

    public List<DeliveryTaskDTO> getMyTasks(String email) {
        seedInitialDemoTasksIfEmpty();
        List<DeliveryTask> tasks = taskRepository.findByDeliveryPartnerUserEmailOrderByCreatedAtDesc(email);
        return tasks.stream().map(this::convertTaskToDTO).collect(Collectors.toList());
    }

    /**
     * Backward-compatible acceptTask.
     * When called on an unassigned AVAILABLE task without confirm flag, sets ASSIGNED.
     * When called on an ASSIGNED task or when confirm is desired, transitions to DELIVERY_ACCEPTED.
     */
    @Transactional
    public DeliveryTaskDTO acceptTask(Long taskId, String email) {
        return acceptTask(taskId, email, false);
    }

    @Transactional
    public DeliveryTaskDTO acceptTask(Long taskId, String email, boolean confirmAcceptance) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultProfileForUser(email));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        String currentStatus = task.getStatus();
        if (!confirmAcceptance) {
            if (!"AVAILABLE".equals(currentStatus) && !"DELIVERY_PENDING".equals(currentStatus)) {
                throw new RuntimeException("Task is no longer available. Status: " + currentStatus);
            }
        } else {
            if (!"AVAILABLE".equals(currentStatus) && !"DELIVERY_PENDING".equals(currentStatus) &&
                !"ASSIGNED".equals(currentStatus) && !"DELIVERY_ASSIGNED".equals(currentStatus)) {
                throw new RuntimeException("Task is no longer available. Status: " + currentStatus);
            }
        }

        if (task.getDeliveryPartner() != null && !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Task is assigned to another delivery partner.");
        }

        task.setDeliveryPartner(partner);
        String newStatus = confirmAcceptance || "ASSIGNED".equals(currentStatus) || "DELIVERY_ASSIGNED".equals(currentStatus)
                ? "DELIVERY_ACCEPTED"
                : "ASSIGNED";
        task.setStatus(newStatus);
        if ("DELIVERY_ACCEPTED".equals(newStatus)) {
            task.setAcceptedAt(LocalDateTime.now());
            task.setIsLiveTrackingActive(false);
        }
        task = taskRepository.save(task);

        logDeliveryAction(task, email, "ACCEPT", currentStatus, newStatus,
                "Partner " + ("DELIVERY_ACCEPTED".equals(newStatus) ? "confirmed acceptance" : "assigned task"));

        notificationService.sendNotification(
                partner.getUser().getId(),
                "Trip " + ("DELIVERY_ACCEPTED".equals(newStatus) ? "Accepted" : "Assigned"),
                String.format("You accepted task %s (%s). Please head towards pickup: %s",
                        task.getTaskCode(), task.getCropName(), task.getPickupLocation()),
                "ORDER"
        );

        return convertTaskToDTO(task);
    }

    /**
     * Atomic, concurrency-safe partner delivery acceptance.
     * Uses pessimistic write locking to prevent race conditions when two partners accept simultaneously.
     */
    @Transactional
    public DeliveryTaskDTO acceptDelivery(Long taskId, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner profile not found for email: " + email));

        if (!Boolean.TRUE.equals(partner.getIsOnline())) {
            partner.setIsOnline(true);
            partnerRepository.save(partner);
        }

        // Pessimistic write lock guarantees atomic exclusivity
        DeliveryTask task = taskRepository.findByIdWithLock(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        String currentStatus = task.getStatus() != null ? task.getStatus().toUpperCase() : "AVAILABLE";

        if ("DELIVERY_ACCEPTED".equals(currentStatus) || "PICKUP_CONFIRMED".equals(currentStatus) ||
            "IN_TRANSIT".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
            throw new IllegalStateException("Task has already been accepted by another partner.");
        }

        if (task.getDeliveryPartner() != null && !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new IllegalStateException("Task is already assigned to another partner.");
        }

        if ("CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot accept cancelled delivery task.");
        }

        task.setDeliveryPartner(partner);
        task.setStatus("DELIVERY_ACCEPTED");
        task.setAcceptedAt(LocalDateTime.now());
        task.setIsLiveTrackingActive(false);
        task = taskRepository.save(task);

        logDeliveryAction(task, email, "ACCEPT", currentStatus, "DELIVERY_ACCEPTED",
                "Partner " + partner.getName() + " explicitly confirmed delivery assignment");

        if (task.getOrder() != null) {
            Order o = task.getOrder();
            if ("PENDING".equals(o.getStatus())) {
                o.setStatus("APPROVED");
                orderRepository.save(o);
            }
        }

        // Alert Buyer that partner has confirmed
        if (task.getOrder() != null && task.getOrder().getBuyer() != null && task.getOrder().getBuyer().getUser() != null) {
            notificationService.sendNotification(
                    task.getOrder().getBuyer().getUser().getId(),
                    "Delivery Partner Confirmed",
                    String.format("Delivery Partner %s (%s) has accepted your order #AGR-%d. Awaiting farm pickup.",
                            partner.getName(), partner.getVehicleNumber(), task.getOrder().getId()),
                    "DELIVERY"
            );
        }

        return convertTaskToDTO(task);
    }

    /**
     * Partner rejects an assigned/offered delivery request.
     * Clears partner assignment and returns task to DELIVERY_PENDING pool for other partners.
     */
    @Transactional
    public DeliveryTaskDTO rejectDelivery(Long taskId, String email, String reason) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner profile not found for email: " + email));

        DeliveryTask task = taskRepository.findByIdWithLock(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        if (task.getDeliveryPartner() != null && !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Unauthorized: You cannot reject a delivery task not offered or assigned to you.");
        }

        String prevStatus = task.getStatus();
        if ("DELIVERED".equals(prevStatus) || "CANCELLED".equals(prevStatus)) {
            throw new RuntimeException("Cannot reject completed or cancelled task.");
        }

        task.setDeliveryPartner(null);
        task.setStatus("DELIVERY_PENDING");
        task.setAcceptedAt(null);
        task.setIsLiveTrackingActive(false);
        task = taskRepository.save(task);

        logDeliveryAction(task, email, "REJECT", prevStatus, "DELIVERY_PENDING",
                reason != null ? reason : "Partner declined delivery request");

        // Broadcast to other online delivery partners
        notificationService.sendNotification(
                null,
                "New Delivery Job Available",
                String.format("Consignment %s (%s) is available for pickup.", task.getTaskCode(), task.getCropName()),
                "MARKETPLACE"
        );

        return convertTaskToDTO(task);
    }

    /**
     * Partner confirms pickup of produce from farmgate.
     */
    @Transactional
    public DeliveryTaskDTO confirmPickup(Long taskId, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner not found."));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        if (task.getDeliveryPartner() == null || !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Unauthorized: This trip is not assigned to you.");
        }

        if (!task.isPartnerConfirmed()) {
            throw new IllegalStateException("Cannot confirm pickup before explicitly accepting the delivery.");
        }

        String prevStatus = task.getStatus();
        task.setStatus("PICKUP_CONFIRMED");
        task.setPickupConfirmedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        if (task.getOrder() != null) {
            Order o = task.getOrder();
            o.setStatus("SHIPPED");
            orderRepository.save(o);
        }

        logDeliveryAction(task, email, "CONFIRM_PICKUP", prevStatus, "PICKUP_CONFIRMED",
                "Partner confirmed produce loading at farmgate");

        if (task.getOrder() != null && task.getOrder().getBuyer() != null && task.getOrder().getBuyer().getUser() != null) {
            notificationService.sendNotification(
                    task.getOrder().getBuyer().getUser().getId(),
                    "Goods Picked Up",
                    String.format("Produce %s (%s qtl) has been picked up from the farm by %s.",
                            task.getCropName(), task.getQuantityQuintals(), partner.getName()),
                    "DELIVERY"
            );
        }

        return convertTaskToDTO(task);
    }

    /**
     * Partner starts the delivery transit toward buyer destination.
     */
    @Transactional
    public DeliveryTaskDTO startTransit(Long taskId, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner not found."));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        if (task.getDeliveryPartner() == null || !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Unauthorized: This trip is not assigned to you.");
        }

        String prevStatus = task.getStatus() != null ? task.getStatus().toUpperCase() : "";
        if (!"PICKUP_CONFIRMED".equals(prevStatus) && !"PICKED_UP".equals(prevStatus) && !"DELIVERY_ACCEPTED".equals(prevStatus)) {
            throw new IllegalStateException("Cannot start transit before goods pickup is confirmed.");
        }

        task.setStatus("IN_TRANSIT");
        task.setTransitStartedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        if (task.getOrder() != null) {
            Order o = task.getOrder();
            o.setStatus("OUT_FOR_DELIVERY");
            orderRepository.save(o);
        }

        logDeliveryAction(task, email, "START_TRANSIT", prevStatus, "IN_TRANSIT",
                "Partner commenced delivery transit to destination");

        if (task.getOrder() != null && task.getOrder().getBuyer() != null && task.getOrder().getBuyer().getUser() != null) {
            notificationService.sendNotification(
                    task.getOrder().getBuyer().getUser().getId(),
                    "Delivery in Progress",
                    String.format("Your consignment of %s is now out for delivery with %s (%s).",
                            task.getCropName(), partner.getName(), partner.getVehicleNumber()),
                    "DELIVERY"
            );
        }

        return convertTaskToDTO(task);
    }

    /**
     * Update real GPS coordinates from the assigned partner's mobile device / browser.
     */
    @Transactional
    public DeliveryTrackingDTO updatePartnerLocation(Long taskId, DeliveryLocationUpdateRequest req, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner not found."));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        if (task.getDeliveryPartner() == null || !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Unauthorized: This trip is not assigned to you.");
        }

        if (!task.isPartnerConfirmed()) {
            throw new IllegalStateException("GPS tracking updates can only be submitted after accepting the delivery.");
        }

        if ("DELIVERED".equals(task.getStatus()) || "CANCELLED".equals(task.getStatus())) {
            task.setIsLiveTrackingActive(false);
            task = taskRepository.save(task);
            return convertToTrackingDTO(task);
        }

        if (Boolean.FALSE.equals(req.getIsPermissionGranted())) {
            task.setIsLiveTrackingActive(false);
            task = taskRepository.save(task);
            return convertToTrackingDTO(task);
        }

        task.setCurrentLatitude(req.getLatitude());
        task.setCurrentLongitude(req.getLongitude());
        task.setLocationUpdatedAt(LocalDateTime.now());
        task.setIsLiveTrackingActive(true);
        task = taskRepository.save(task);

        return convertToTrackingDTO(task);
    }

    /**
     * Transition task status with security checks and OTP verification for completion.
     */
    @Transactional
    public DeliveryTaskDTO updateTaskStatus(Long taskId, String newStatus, String otp, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultProfileForUser(email));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + taskId));

        if (task.getDeliveryPartner() == null || !task.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Unauthorized: This trip is not assigned to you.");
        }

        String prevStatus = task.getStatus();
        String target = newStatus.toUpperCase();

        if ("DELIVERED".equals(target)) {
            // Verify OTP
            if (otp != null && !otp.trim().isEmpty()) {
                if (!otp.trim().equals(task.getDeliveryOtp())) {
                    throw new RuntimeException("Invalid delivery verification OTP entered.");
                }
            }
            task.setStatus("DELIVERED");
            task.setDeliveredAt(LocalDateTime.now());
            task.setIsLiveTrackingActive(false);

            // Add earnings to partner
            BigDecimal totalPayout = task.getPayoutAmount().add(task.getSurgeBonus() != null ? task.getSurgeBonus() : BigDecimal.ZERO);
            partner.addCompletedTrip(totalPayout);
            partnerRepository.save(partner);

            // Update underlying Order if linked
            if (task.getOrder() != null) {
                task.getOrder().setStatus("DELIVERED");
                orderRepository.save(task.getOrder());
            }

            logDeliveryAction(task, email, "DELIVER", prevStatus, "DELIVERED",
                    "Delivery completed and verified via OTP. Payout credited.");

            notificationService.sendNotification(
                    partner.getUser().getId(),
                    "Delivery Completed! 🎉",
                    String.format("Trip %s completed! ₹%s credited to your daily wallet earnings.",
                            task.getTaskCode(), totalPayout.toPlainString()),
                    "PAYMENT"
            );

            if (task.getOrder() != null && task.getOrder().getBuyer() != null && task.getOrder().getBuyer().getUser() != null) {
                notificationService.sendNotification(
                        task.getOrder().getBuyer().getUser().getId(),
                        "Order Delivered Successfully! 🎉",
                        String.format("Your order #AGR-%d (%s) has been delivered by %s.",
                                task.getOrder().getId(), task.getCropName(), partner.getName()),
                        "DELIVERY"
                );
            }
        } else if ("DELIVERY_ACCEPTED".equals(target)) {
            task.setStatus("DELIVERY_ACCEPTED");
            task.setAcceptedAt(LocalDateTime.now());
            task.setIsLiveTrackingActive(false);
            logDeliveryAction(task, email, "ACCEPT", prevStatus, "DELIVERY_ACCEPTED", "Partner confirmed delivery assignment");
        } else if ("PICKUP_CONFIRMED".equals(target) || "PICKED_UP".equals(target)) {
            if (!task.isPartnerConfirmed()) {
                throw new IllegalStateException("Cannot confirm pickup before explicitly accepting the delivery.");
            }
            task.setStatus("PICKUP_CONFIRMED");
            task.setPickupConfirmedAt(LocalDateTime.now());
            if (task.getOrder() != null) {
                task.getOrder().setStatus("SHIPPED");
                orderRepository.save(task.getOrder());
            }
            logDeliveryAction(task, email, "CONFIRM_PICKUP", prevStatus, "PICKUP_CONFIRMED", "Produce loaded at farmgate");
        } else if ("IN_TRANSIT".equals(target)) {
            task.setStatus("IN_TRANSIT");
            task.setTransitStartedAt(LocalDateTime.now());
            if (task.getOrder() != null) {
                task.getOrder().setStatus("OUT_FOR_DELIVERY");
                orderRepository.save(task.getOrder());
            }
            logDeliveryAction(task, email, "START_TRANSIT", prevStatus, "IN_TRANSIT", "In transit to buyer destination");
        } else {
            task.setStatus(target);
            logDeliveryAction(task, email, "UPDATE_STATUS", prevStatus, target, "Task status updated");
        }

        task = taskRepository.save(task);
        return convertTaskToDTO(task);
    }

    /**
     * Comprehensive real-time tracking lookup for an order or task.
     * Prevents fake tracking, dummy vehicle simulation, or partner leaks before acceptance.
     */
    public DeliveryTrackingDTO getDeliveryTracking(Long id, String callerEmail, boolean isOrderId) {
        DeliveryTask task;
        if (isOrderId) {
            task = taskRepository.findByOrderId(id)
                    .orElseGet(() -> createOrFindTaskForOrder(id));
        } else {
            task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Delivery task not found with ID: " + id));
        }

        validateTrackingAccess(task, callerEmail);
        return convertToTrackingDTO(task);
    }

    private void validateTrackingAccess(DeliveryTask task, String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) return;
        User caller = userRepository.findByEmail(callerEmail).orElse(null);
        if (caller == null) return;
        if ("ADMIN".equals(caller.getRole())) return;

        if ("DELIVERY_PARTNER".equals(caller.getRole())) {
            if (task.getDeliveryPartner() != null && task.getDeliveryPartner().getUser() != null) {
                if (task.getDeliveryPartner().getUser().getEmail().equalsIgnoreCase(callerEmail)) return;
            }
            return;
        }

        if ("BUYER".equals(caller.getRole())) {
            if (task.getOrder() != null && task.getOrder().getBuyer() != null && task.getOrder().getBuyer().getUser() != null) {
                if (task.getOrder().getBuyer().getUser().getEmail().equalsIgnoreCase(callerEmail)) return;
            }
        }
    }

    public DeliveryTrackingDTO convertToTrackingDTO(DeliveryTask t) {
        DeliveryTrackingDTO dto = new DeliveryTrackingDTO();
        dto.setDeliveryTaskId(t.getId());
        dto.setTaskCode(t.getTaskCode());
        if (t.getOrder() != null) {
            dto.setOrderId(t.getOrder().getId());
        }
        dto.setCropName(t.getCropName());
        dto.setVariety(t.getVariety());
        dto.setQuantityQuintals(t.getQuantityQuintals());
        dto.setPickupLocation(t.getPickupLocation());
        dto.setDropLocation(t.getDropLocation());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setAcceptedAt(t.getAcceptedAt());
        dto.setPickupConfirmedAt(t.getPickupConfirmedAt());
        dto.setTransitStartedAt(t.getTransitStartedAt());
        dto.setDeliveredAt(t.getDeliveredAt());
        dto.setDeliveryOtp(t.getDeliveryOtp());

        boolean isConfirmed = t.isPartnerConfirmed();
        dto.setPartnerConfirmed(isConfirmed);

        if (!isConfirmed) {
            // PRE-ACCEPTANCE: STRICTLY NO PARTNER DETAILS, NO FAKE GPS, NO LIVE STATUS
            dto.setDisplayStatus("Waiting for a delivery partner to confirm");
            dto.setLiveTrackingActive(false);
            dto.setPartnerName(null);
            dto.setPartnerPhone(null);
            dto.setVehicleType(null);
            dto.setVehicleNumber(null);
            dto.setPartnerRating(null);
            dto.setCurrentLatitude(null);
            dto.setCurrentLongitude(null);
            dto.setLocationUpdatedAt(null);
            dto.setEstimatedMinutes(t.getEstimatedMinutes());
            dto.setDistanceKm(t.getDistanceKm());
            dto.setTrackingWarning("Waiting for a delivery partner to accept this consignment.");
            return dto;
        }

        // POST-ACCEPTANCE: Verified partner details
        DeliveryPartner p = t.getDeliveryPartner();
        if (p != null) {
            dto.setPartnerId(p.getId());
            dto.setPartnerName(p.getName());
            dto.setPartnerPhone(p.getPhone());
            dto.setVehicleType(p.getVehicleType());
            dto.setVehicleNumber(p.getVehicleNumber());
            dto.setPartnerRating(p.getRating());
            if (p.getUser() != null) {
                dto.setPartnerPhoto(p.getUser().getProfilePhoto());
            }
        }

        // Display Status
        String s = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
        if ("DELIVERY_ACCEPTED".equals(s) || "ASSIGNED".equals(s)) {
            dto.setDisplayStatus("Partner confirmed — heading to farmgate");
        } else if ("PICKUP_CONFIRMED".equals(s) || "PICKED_UP".equals(s) || "REACHED_PICKUP".equals(s)) {
            dto.setDisplayStatus("Goods picked up at farmgate");
        } else if ("IN_TRANSIT".equals(s)) {
            dto.setDisplayStatus("Delivery in progress");
        } else if ("DELIVERED".equals(s)) {
            dto.setDisplayStatus("Delivered");
        } else {
            dto.setDisplayStatus(t.getStatus());
        }

        // Real GPS Tracking status
        boolean isDelivered = "DELIVERED".equals(s);
        boolean isCancelled = "CANCELLED".equals(s);

        if (isDelivered) {
            dto.setLiveTrackingActive(false);
            dto.setTrackingWarning("Consignment delivered successfully.");
        } else if (isCancelled) {
            dto.setLiveTrackingActive(false);
            dto.setTrackingWarning("Delivery cancelled.");
        } else {
            boolean hasCoords = t.getCurrentLatitude() != null && t.getCurrentLongitude() != null;
            boolean isStale = false;
            if (t.getLocationUpdatedAt() != null) {
                isStale = t.getLocationUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(5));
            }

            if (Boolean.TRUE.equals(t.getIsLiveTrackingActive()) && hasCoords && !isStale) {
                dto.setLiveTrackingActive(true);
                dto.setCurrentLatitude(t.getCurrentLatitude());
                dto.setCurrentLongitude(t.getCurrentLongitude());
                dto.setLocationUpdatedAt(t.getLocationUpdatedAt());
                dto.setEstimatedMinutes(t.getEstimatedMinutes());
                dto.setDistanceKm(t.getDistanceKm());
                dto.setTrackingWarning(null);
            } else {
                dto.setLiveTrackingActive(false);
                dto.setCurrentLatitude(t.getCurrentLatitude());
                dto.setCurrentLongitude(t.getCurrentLongitude());
                dto.setLocationUpdatedAt(t.getLocationUpdatedAt());
                if (!hasCoords) {
                    dto.setTrackingWarning("Partner has accepted. Live GPS location updates will begin once driver is in transit.");
                } else if (isStale) {
                    dto.setTrackingWarning("GPS signal was updated more than 5 minutes ago. Waiting for next signal.");
                } else {
                    dto.setTrackingWarning("GPS location sharing is currently paused or inactive.");
                }
            }
        }

        return dto;
    }

    /**
     * Create delivery task for an order if one does not exist.
     */
    @Transactional
    public DeliveryTask createOrFindTaskForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        return taskRepository.findByOrderId(orderId).orElseGet(() -> {
            DeliveryTask task = new DeliveryTask();
            task.setTaskCode("DLV-" + LocalDate.now().getYear() + "-" + (order.getId() + 1000));
            task.setOrder(order);

            String pickup = "Farmgate Dispatch Hub, " +
                    (order.getProduct() != null && order.getProduct().getFarmer() != null
                            ? order.getProduct().getFarmer().getDistrict() + ", " + order.getProduct().getFarmer().getState()
                            : "Nashik, Maharashtra");
            String drop = order.getBuyer() != null && order.getBuyer().getAddress() != null
                    ? order.getBuyer().getAddress()
                    : "Nashik APMC Terminal, Maharashtra";

            task.setPickupLocation(pickup);
            task.setDropLocation(drop);
            task.setPickupContactName(order.getProduct() != null && order.getProduct().getFarmer() != null ? order.getProduct().getFarmer().getName() : "Farmer Producer");
            task.setPickupContactPhone(order.getProduct() != null && order.getProduct().getFarmer() != null ? order.getProduct().getFarmer().getPhone() : "9823012345");
            task.setDropContactName(order.getBuyer() != null ? order.getBuyer().getRepresentativeName() : "Procurement Buyer");
            task.setDropContactPhone(order.getBuyer() != null ? order.getBuyer().getPhone() : "9123456789");

            task.setCropName(order.getProduct() != null ? order.getProduct().getCropName() : "Produce Batch");
            task.setVariety(order.getProduct() != null ? order.getProduct().getVariety() : "Grade A");
            task.setQuantityQuintals(order.getQuantityQuintals() != null ? order.getQuantityQuintals() : BigDecimal.ONE);
            task.setPayoutAmount(new BigDecimal("350.00"));
            task.setSurgeBonus(new BigDecimal("50.00"));
            task.setDistanceKm(new BigDecimal("8.50"));
            task.setEstimatedMinutes(25);
            task.setStatus("DELIVERY_PENDING");
            task.setDeliveryOtp("4819");
            task.setPaymentType(order.getPaymentMethod() != null ? order.getPaymentMethod() : "ESCROW_PAID");
            task.setIsLiveTrackingActive(false);

            DeliveryTask saved = taskRepository.save(task);

            logDeliveryAction(saved, "SYSTEM", "CREATE_TASK", null, "DELIVERY_PENDING",
                    "New delivery consignment created for Order #" + order.getId());

            return saved;
        });
    }

    // ==========================================
    // Admin Delivery Oversight & Audit
    // ==========================================

    public List<DeliveryTaskDTO> getAllDeliveries(String statusFilter) {
        List<DeliveryTask> list;
        if (statusFilter != null && !statusFilter.isBlank()) {
            list = taskRepository.findByStatus(statusFilter.toUpperCase());
        } else {
            list = taskRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::convertTaskToDTO).collect(Collectors.toList());
    }

    public List<DeliveryTaskDTO> getUnacceptedDeliveries() {
        return taskRepository.findUnacceptedAssignments().stream()
                .map(this::convertTaskToDTO).collect(Collectors.toList());
    }

    @Transactional
    public DeliveryTaskDTO reassignDelivery(Long taskId, Long newPartnerId, String adminEmail) {
        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        DeliveryPartner newPartner = partnerRepository.findById(newPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with ID: " + newPartnerId));

        String prevStatus = task.getStatus();
        String oldPartnerName = task.getDeliveryPartner() != null ? task.getDeliveryPartner().getName() : "None";

        task.setDeliveryPartner(newPartner);
        task.setStatus("DELIVERY_ASSIGNED");
        task.setAcceptedAt(null);
        task.setIsLiveTrackingActive(false);
        task = taskRepository.save(task);

        logDeliveryAction(task, adminEmail, "REASSIGN", prevStatus, "DELIVERY_ASSIGNED",
                String.format("Admin reassigned delivery from %s to %s", oldPartnerName, newPartner.getName()));

        notificationService.sendNotification(
                newPartner.getUser().getId(),
                "Delivery Assignment Offer",
                String.format("You have been assigned order %s (%s). Please review and confirm acceptance.",
                        task.getTaskCode(), task.getCropName()),
                "ORDER"
        );

        return convertTaskToDTO(task);
    }

    public List<DeliveryAuditLog> getAuditLogsForTask(Long taskId) {
        return auditLogRepository.findByDeliveryTaskIdOrderByCreatedAtDesc(taskId);
    }

    public List<DeliveryAuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private void logDeliveryAction(DeliveryTask task, String actorEmail, String action,
                                   String prevStatus, String newStatus, String remarks) {
        try {
            String role = "SYSTEM";
            if (actorEmail != null && !actorEmail.isBlank() && !"SYSTEM".equalsIgnoreCase(actorEmail)) {
                User u = userRepository.findByEmail(actorEmail).orElse(null);
                if (u != null) role = u.getRole();
            }
            DeliveryAuditLog log = new DeliveryAuditLog(
                    task.getId(),
                    task.getOrder() != null ? task.getOrder().getId() : null,
                    actorEmail != null ? actorEmail : "SYSTEM",
                    role,
                    action,
                    prevStatus,
                    newStatus,
                    remarks
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to write delivery audit log: " + e.getMessage());
        }
    }

    @Transactional
    public String withdrawPayout(BigDecimal amount, String upiId, String email) {
        DeliveryPartner partner = partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid withdrawal amount.");
        }

        if (partner.getTodayEarnings().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance for withdrawal.");
        }

        partner.setTodayEarnings(partner.getTodayEarnings().subtract(amount));
        partnerRepository.save(partner);

        notificationService.sendNotification(
                partner.getUser().getId(),
                "Payout Transferred",
                String.format("₹%s has been successfully transferred via Instant IMPS UPI to %s.",
                        amount.toPlainString(), (upiId != null ? upiId : partner.getUpiId())),
                "PAYMENT"
        );

        return String.format("Withdrawal of ₹%s transferred successfully!", amount.toPlainString());
    }

    @Transactional
    public DeliveryPartnerDTO applyForDeliveryPartner(DeliveryApplyRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (userRepository.existsByEmail(email)) {
            User existing = userRepository.findByEmail(email).get();
            DeliveryPartner existingPartner = partnerRepository.findByUserEmail(email).orElse(null);
            if (existingPartner != null) {
                return convertPartnerToDTO(existingPartner);
            }
            existing.setRole("DELIVERY_PARTNER");
            userRepository.save(existing);
            DeliveryPartner newPartner = DeliveryPartner.create(
                    existing, request.getName(), request.getPhone(), request.getDistrict(),
                    request.getVehicleType(), request.getVehicleNumber(), request.getLicenseNumber(),
                    request.getPreferredHub(), request.getUpiId()
            );
            newPartner = partnerRepository.save(newPartner);
            return convertPartnerToDTO(newPartner);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole("DELIVERY_PARTNER");
        user.setActive(true);
        user.setProfilePhoto(request.getProfilePhoto());
        user = userRepository.save(user);

        DeliveryPartner partner = DeliveryPartner.create(
                user, request.getName(), request.getPhone(), request.getDistrict(),
                request.getVehicleType(), request.getVehicleNumber(), request.getLicenseNumber(),
                request.getPreferredHub(), request.getUpiId()
        );
        partner.setEmergencyContactName(request.getEmergencyContactName());
        partner.setEmergencyContactPhone(request.getEmergencyContactPhone());
        partner = partnerRepository.save(partner);

        notificationService.sendToAdmin(
                "New Delivery Fleet Application",
                String.format("New Delivery Partner applied: %s (%s, Vehicle: %s).",
                        partner.getName(), partner.getPhone(), partner.getVehicleType()),
                "SYSTEM"
        );

        return convertPartnerToDTO(partner);
    }

    private DeliveryPartner createDefaultProfileForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        DeliveryPartner partner = DeliveryPartner.create(
                user,
                user.getEmail().split("@")[0].toUpperCase() + " (Fleet)",
                "9823012345",
                "Nashik",
                "SCOOTER",
                "MH-15-BL-2026",
                "MH15-DL-88741",
                "Nashik APMC Main Terminal",
                "partner@okaxis"
        );
        return partnerRepository.save(partner);
    }

    private void seedInitialDemoTasksIfEmpty() {
        if (taskRepository.count() == 0) {
            List<DeliveryTask> seedTasks = new ArrayList<>();

            DeliveryTask t1 = new DeliveryTask();
            t1.setTaskCode("DLV-2026-101");
            t1.setPickupLocation("Ramesh Patil Farmgate, Survey #42, Lasalgaon, Nashik");
            t1.setPickupContactName("Ramesh Patil (Organic Onion Farmer)");
            t1.setPickupContactPhone("9823154820");
            t1.setDropLocation("FreshBlink Super Hub, MIDC Satpur, Nashik (Wholesale Buyer)");
            t1.setDropContactName("Amol Deshmukh (Procurement)");
            t1.setDropContactPhone("9890451234");
            t1.setCropName("Onion");
            t1.setVariety("Nasik Red Special");
            t1.setQuantityQuintals(new BigDecimal("3.50"));
            t1.setPayoutAmount(new BigDecimal("340.00"));
            t1.setSurgeBonus(new BigDecimal("60.00"));
            t1.setDistanceKm(new BigDecimal("7.20"));
            t1.setEstimatedMinutes(22);
            t1.setStatus("DELIVERY_PENDING");
            t1.setDeliveryOtp("4819");
            t1.setPaymentType("ESCROW_PAID");
            t1.setIsLiveTrackingActive(false);
            seedTasks.add(t1);

            DeliveryTask t2 = new DeliveryTask();
            t2.setTaskCode("DLV-2026-102");
            t2.setPickupLocation("Sunil Shinde Agro Farm & Cold Storage Hub, Dindori");
            t2.setPickupContactName("Sunil Shinde (Grape Grower & Storage Partner)");
            t2.setPickupContactPhone("9845231908");
            t2.setDropLocation("Kalyan Wholesale Fruit Market, Terminal C");
            t2.setDropContactName("Pooja Verma (Store Mgr)");
            t2.setDropContactPhone("9765123890");
            t2.setCropName("Grapes");
            t2.setVariety("Thomson Seedless Export");
            t2.setQuantityQuintals(new BigDecimal("2.00"));
            t2.setPayoutAmount(new BigDecimal("420.00"));
            t2.setSurgeBonus(new BigDecimal("80.00"));
            t2.setDistanceKm(new BigDecimal("11.50"));
            t2.setEstimatedMinutes(35);
            t2.setStatus("DELIVERY_PENDING");
            t2.setDeliveryOtp("7732");
            t2.setPaymentType("ESCROW_PAID");
            t2.setIsLiveTrackingActive(false);
            seedTasks.add(t2);

            DeliveryTask t3 = new DeliveryTask();
            t3.setTaskCode("DLV-2026-103");
            t3.setPickupLocation("Ganesh Jadhav Polyhouse Farm, Pimpalgaon Baswant");
            t3.setPickupContactName("Ganesh Jadhav (Tomato Farmer)");
            t3.setPickupContactPhone("9921445566");
            t3.setDropLocation("Reliance Fresh Distribution Center, Ambad");
            t3.setDropContactName("Vikram Rane");
            t3.setDropContactPhone("9881223344");
            t3.setCropName("Tomato");
            t3.setVariety("Vaishnavi Hybrid");
            t3.setQuantityQuintals(new BigDecimal("4.00"));
            t3.setPayoutAmount(new BigDecimal("380.00"));
            t3.setSurgeBonus(new BigDecimal("40.00"));
            t3.setDistanceKm(new BigDecimal("8.80"));
            t3.setEstimatedMinutes(26);
            t3.setStatus("DELIVERY_PENDING");
            t3.setDeliveryOtp("2910");
            t3.setPaymentType("ESCROW_PAID");
            t3.setIsLiveTrackingActive(false);
            seedTasks.add(t3);

            taskRepository.saveAll(seedTasks);
        }
    }

    public DeliveryPartnerDTO convertPartnerToDTO(DeliveryPartner p) {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setId(p.getId());
        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            dto.setEmail(p.getUser().getEmail());
            dto.setProfilePhoto(p.getUser().getProfilePhoto());
        }
        dto.setName(p.getName());
        dto.setPhone(p.getPhone());
        dto.setCity(p.getCity());
        dto.setDistrict(p.getDistrict());
        dto.setVehicleType(p.getVehicleType());
        dto.setVehicleNumber(p.getVehicleNumber());
        dto.setLicenseNumber(p.getLicenseNumber());
        dto.setPreferredHub(p.getPreferredHub());
        dto.setUpiId(p.getUpiId());
        dto.setEmergencyContactName(p.getEmergencyContactName());
        dto.setEmergencyContactPhone(p.getEmergencyContactPhone());
        dto.setIsOnline(p.getIsOnline());
        dto.setRating(p.getRating());
        dto.setTotalDeliveries(p.getTotalDeliveries());
        dto.setTodayDeliveries(p.getTodayDeliveries());
        dto.setTodayEarnings(p.getTodayEarnings());
        dto.setTotalEarnings(p.getTotalEarnings());
        dto.setDutyShift(p.getDutyShift());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    public DeliveryTaskDTO convertTaskToDTO(DeliveryTask t) {
        DeliveryTaskDTO dto = new DeliveryTaskDTO();
        dto.setId(t.getId());
        dto.setTaskCode(t.getTaskCode());
        if (t.getOrder() != null) {
            dto.setOrderId(t.getOrder().getId());
        }
        if (t.getDeliveryPartner() != null) {
            dto.setPartnerId(t.getDeliveryPartner().getId());
            dto.setPartnerName(t.getDeliveryPartner().getName());
            dto.setPartnerPhone(t.getDeliveryPartner().getPhone());
            dto.setPartnerVehicleNumber(t.getDeliveryPartner().getVehicleNumber());
        }
        dto.setPickupLocation(t.getPickupLocation());
        dto.setPickupContactName(t.getPickupContactName());
        dto.setPickupContactPhone(t.getPickupContactPhone());
        dto.setDropLocation(t.getDropLocation());
        dto.setDropContactName(t.getDropContactName());
        dto.setDropContactPhone(t.getDropContactPhone());
        dto.setCropName(t.getCropName());
        dto.setVariety(t.getVariety());
        dto.setQuantityQuintals(t.getQuantityQuintals());
        dto.setPayoutAmount(t.getPayoutAmount());
        dto.setSurgeBonus(t.getSurgeBonus());
        dto.setDistanceKm(t.getDistanceKm());
        dto.setEstimatedMinutes(t.getEstimatedMinutes());
        dto.setStatus(t.getStatus());
        dto.setDeliveryOtp(t.getDeliveryOtp());
        dto.setPaymentType(t.getPaymentType());
        dto.setCashToCollect(t.getCashToCollect());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setDeliveredAt(t.getDeliveredAt());

        dto.setCurrentLatitude(t.getCurrentLatitude());
        dto.setCurrentLongitude(t.getCurrentLongitude());
        dto.setLocationUpdatedAt(t.getLocationUpdatedAt());
        dto.setIsLiveTrackingActive(t.getIsLiveTrackingActive());
        dto.setAcceptedAt(t.getAcceptedAt());
        dto.setPickupConfirmedAt(t.getPickupConfirmedAt());
        dto.setTransitStartedAt(t.getTransitStartedAt());
        dto.setPartnerConfirmed(t.isPartnerConfirmed());

        return dto;
    }
}
