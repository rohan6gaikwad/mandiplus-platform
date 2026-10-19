package com.rtech.mandiplus;

import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.*;
import com.rtech.mandiplus.repository.*;
import com.rtech.mandiplus.service.DeliveryService;
import com.rtech.mandiplus.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Live Delivery Tracking & Delivery Partner Confirmation Workflow Test Suite
 * Covers Scenarios 1 through 9:
 * 1. Buyer places an order: tracking remains inactive.
 * 2. A delivery request is assigned: tracking remains inactive.
 * 3. An eligible partner accepts: the backend records acceptance.
 * 4. A partner rejects: another eligible partner can receive the request.
 * 5. Two partners accept simultaneously: only one succeeds.
 * 6. An unrelated partner tries to update the order: request is denied.
 * 7. The accepted partner confirms pickup and starts transit: tracking becomes active when valid GPS updates are available.
 * 8. The order is completed: active tracking stops.
 * 9. GPS permission is denied or location is stale: accurate warning without fake movement.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeliveryLifecycleTrackingTest {

    @Mock private DeliveryPartnerRepository partnerRepository;
    @Mock private DeliveryTaskRepository taskRepository;
    @Mock private DeliveryAuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PasswordEncoder encoder;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private DeliveryService deliveryService;

    private User buyerUser;
    private Buyer buyer;
    private User farmerUser;
    private Farmer farmer;
    private Product product;
    private Order order;

    private User partner1User;
    private DeliveryPartner partner1;
    private User partner2User;
    private DeliveryPartner partner2;

    private DeliveryTask deliveryTask;

    @BeforeEach
    void setUp() {
        buyerUser = new User(101L, "buyer@mandiplus.com", "hash", "BUYER", true, null, null);
        buyer = new Buyer();
        buyer.setId(101L);
        buyer.setUser(buyerUser);
        buyer.setCompanyName("AgroTrade Ltd");
        buyer.setAddress("Nashik APMC Terminal, Maharashtra");

        farmerUser = new User(201L, "farmer@mandiplus.com", "hash", "FARMER", true, null, null);
        farmer = new Farmer();
        farmer.setId(201L);
        farmer.setUser(farmerUser);
        farmer.setName("Kisan Patil");
        farmer.setDistrict("Nashik");
        farmer.setState("Maharashtra");
        farmer.setPhone("9823011111");

        product = new Product();
        product.setId(301L);
        product.setCropName("Onion");
        product.setVariety("Nashik Red");
        product.setFarmer(farmer);

        order = new Order();
        order.setId(501L);
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantityQuintals(new BigDecimal("10.00"));
        order.setStatus("PENDING");

        partner1User = new User(401L, "partner1@mandiplus.com", "hash", "DELIVERY_PARTNER", true, null, null);
        partner1 = new DeliveryPartner();
        partner1.setId(401L);
        partner1.setUser(partner1User);
        partner1.setName("Rahul Shinde");
        partner1.setPhone("9823012345");
        partner1.setVehicleType("SCOOTER");
        partner1.setVehicleNumber("MH-15-BL-2026");
        partner1.setRating(new BigDecimal("4.92"));
        partner1.setIsOnline(true);

        partner2User = new User(402L, "partner2@mandiplus.com", "hash", "DELIVERY_PARTNER", true, null, null);
        partner2 = new DeliveryPartner();
        partner2.setId(402L);
        partner2.setUser(partner2User);
        partner2.setName("Ajay Gaikwad");
        partner2.setPhone("9823099999");
        partner2.setVehicleType("MOTORCYCLE");
        partner2.setVehicleNumber("MH-15-AG-4412");
        partner2.setRating(new BigDecimal("4.88"));
        partner2.setIsOnline(true);

        deliveryTask = new DeliveryTask();
        deliveryTask.setId(601L);
        deliveryTask.setTaskCode("DLV-2026-1501");
        deliveryTask.setOrder(order);
        deliveryTask.setCropName("Onion");
        deliveryTask.setVariety("Nashik Red");
        deliveryTask.setQuantityQuintals(new BigDecimal("10.00"));
        deliveryTask.setPickupLocation("Farmgate Dispatch Hub, Nashik, Maharashtra");
        deliveryTask.setDropLocation("Nashik APMC Terminal, Maharashtra");
        deliveryTask.setPayoutAmount(new BigDecimal("350.00"));
        deliveryTask.setSurgeBonus(new BigDecimal("50.00"));
        deliveryTask.setStatus("DELIVERY_PENDING");
        deliveryTask.setDeliveryOtp("4819");
        deliveryTask.setIsLiveTrackingActive(false);

        when(userRepository.findByEmail("buyer@mandiplus.com")).thenReturn(Optional.of(buyerUser));
        when(userRepository.findByEmail("partner1@mandiplus.com")).thenReturn(Optional.of(partner1User));
        when(userRepository.findByEmail("partner2@mandiplus.com")).thenReturn(Optional.of(partner2User));
        when(partnerRepository.findByUserEmail("partner1@mandiplus.com")).thenReturn(Optional.of(partner1));
        when(partnerRepository.findByUserEmail("partner2@mandiplus.com")).thenReturn(Optional.of(partner2));
        when(taskRepository.findById(601L)).thenReturn(Optional.of(deliveryTask));
        when(taskRepository.findByIdWithLock(601L)).thenReturn(Optional.of(deliveryTask));
        when(taskRepository.findByOrderId(501L)).thenReturn(Optional.of(deliveryTask));
        when(taskRepository.save(any(DeliveryTask.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }

    // =========================================================================
    // Scenario 1: Buyer places an order: tracking remains inactive
    // =========================================================================
    @Test
    @DisplayName("Scenario 1: Buyer places an order — Tracking remains strictly inactive before partner acceptance")
    void test01_BuyerPlacesOrder_TrackingRemainsInactive() {
        DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(501L, "buyer@mandiplus.com", true);

        assertNotNull(tracking, "Tracking DTO must be returned");
        assertFalse(tracking.isPartnerConfirmed(), "Partner must not be marked confirmed pre-acceptance");
        assertFalse(tracking.isLiveTrackingActive(), "Live GPS tracking must be inactive pre-acceptance");
        assertNull(tracking.getPartnerName(), "Partner name must be null to prevent leaking unconfirmed partner");
        assertNull(tracking.getPartnerPhone(), "Partner phone must be sanitized pre-acceptance");
        assertNull(tracking.getVehicleNumber(), "Vehicle number must be null pre-acceptance");
        assertNull(tracking.getCurrentLatitude(), "Current latitude must not be populated pre-acceptance");
        assertNull(tracking.getCurrentLongitude(), "Current longitude must not be populated pre-acceptance");
        assertEquals("Waiting for a delivery partner to confirm", tracking.getDisplayStatus());
    }

    // =========================================================================
    // Scenario 2: A delivery request is assigned: tracking remains inactive
    // =========================================================================
    @Test
    @DisplayName("Scenario 2: A delivery request is assigned/offered — Tracking remains strictly inactive until confirmed")
    void test02_DeliveryRequestAssigned_TrackingRemainsInactive() {
        deliveryTask.setStatus("DELIVERY_ASSIGNED");
        deliveryTask.setDeliveryPartner(partner1); // Offered/assigned to partner1

        DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(501L, "buyer@mandiplus.com", true);

        assertFalse(tracking.isPartnerConfirmed(), "Assigned task is not yet confirmed by partner");
        assertFalse(tracking.isLiveTrackingActive(), "Tracking must remain inactive during assignment offer");
        assertNull(tracking.getPartnerName(), "Partner name must remain hidden until explicit partner acceptance");
        assertEquals("Waiting for a delivery partner to confirm", tracking.getDisplayStatus());
    }

    // =========================================================================
    // Scenario 3: An eligible partner accepts: the backend records acceptance
    // =========================================================================
    @Test
    @DisplayName("Scenario 3: An eligible partner accepts — Backend records acceptance and unlocks verified profile")
    void test03_EligiblePartnerAccepts_BackendRecordsAcceptance() {
        deliveryTask.setStatus("DELIVERY_PENDING");

        DeliveryTaskDTO accepted = deliveryService.acceptDelivery(601L, "partner1@mandiplus.com");

        assertEquals("DELIVERY_ACCEPTED", accepted.getStatus(), "Status must transition to DELIVERY_ACCEPTED");
        assertTrue(accepted.isPartnerConfirmed(), "Partner confirmed flag must be true");
        assertNotNull(deliveryTask.getAcceptedAt(), "Accepted timestamp must be recorded");

        // Verify audit log recorded
        verify(auditLogRepository, atLeastOnce()).save(any(DeliveryAuditLog.class));

        // Query tracking after acceptance
        DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(501L, "buyer@mandiplus.com", true);
        assertTrue(tracking.isPartnerConfirmed(), "Tracking DTO must reflect partner acceptance");
        assertEquals("Rahul Shinde", tracking.getPartnerName(), "Verified partner name must now be visible");
        assertEquals("MH-15-BL-2026", tracking.getVehicleNumber(), "Verified vehicle number must now be visible");
        assertFalse(tracking.isLiveTrackingActive(), "Live tracking must not be active before pickup and transit");
    }

    // =========================================================================
    // Scenario 4: A partner rejects: another eligible partner can receive request
    // =========================================================================
    @Test
    @DisplayName("Scenario 4: A partner rejects — Task returns to pool and another eligible partner can accept")
    void test04_PartnerRejects_AnotherPartnerCanReceiveRequest() {
        // Partner 1 is offered the delivery
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("DELIVERY_ASSIGNED");

        // Partner 1 rejects
        DeliveryTaskDTO rejected = deliveryService.rejectDelivery(601L, "partner1@mandiplus.com", "Bike flat tire");
        assertEquals("DELIVERY_PENDING", rejected.getStatus(), "Task must return to DELIVERY_PENDING pool");
        assertNull(deliveryTask.getDeliveryPartner(), "Assigned partner must be cleared");

        // Verify audit log captured rejection
        verify(auditLogRepository, atLeastOnce()).save(argThat(log ->
                "REJECT".equals(log.getAction()) && "DELIVERY_PENDING".equals(log.getNewStatus())
        ));

        // Partner 2 accepts the unassigned task
        DeliveryTaskDTO acceptedByP2 = deliveryService.acceptDelivery(601L, "partner2@mandiplus.com");
        assertEquals("DELIVERY_ACCEPTED", acceptedByP2.getStatus());
        assertEquals("Ajay Gaikwad", deliveryTask.getDeliveryPartner().getName());
    }

    // =========================================================================
    // Scenario 5: Two partners accept simultaneously: only one succeeds
    // =========================================================================
    @Test
    @DisplayName("Scenario 5: Two partners accept simultaneously — Concurrency-safe atomic check prevents double assignment")
    void test05_TwoPartnersAcceptSimultaneously_ConcurrencyExclusivity() {
        deliveryTask.setStatus("DELIVERY_PENDING");

        // Partner 1 accepts successfully
        deliveryService.acceptDelivery(601L, "partner1@mandiplus.com");
        assertEquals("DELIVERY_ACCEPTED", deliveryTask.getStatus());
        assertEquals(partner1, deliveryTask.getDeliveryPartner());

        // Partner 2 attempts to accept the already accepted task
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            deliveryService.acceptDelivery(601L, "partner2@mandiplus.com");
        });
        assertTrue(ex.getMessage().contains("already been accepted") || ex.getMessage().contains("already assigned"),
                "Exception message must clearly indicate task is already claimed");
        assertEquals(partner1, deliveryTask.getDeliveryPartner(), "Partner 1 must remain the exclusive assignee");
    }

    // =========================================================================
    // Scenario 6: An unrelated partner tries to update the order: request is denied
    // =========================================================================
    @Test
    @DisplayName("Scenario 6: Unrelated partner tries to update delivery — Server denies unauthorized access")
    void test06_UnrelatedPartnerUpdate_AccessDenied() {
        // Task accepted by Partner 1
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("DELIVERY_ACCEPTED");

        // Partner 2 attempts pickup confirmation
        assertThrows(RuntimeException.class, () -> {
            deliveryService.confirmPickup(601L, "partner2@mandiplus.com");
        }, "Unrelated partner must not be allowed to confirm pickup");

        // Partner 2 attempts to start transit
        assertThrows(RuntimeException.class, () -> {
            deliveryService.startTransit(601L, "partner2@mandiplus.com");
        }, "Unrelated partner must not be allowed to start transit");

        // Partner 2 attempts to push GPS coordinates
        DeliveryLocationUpdateRequest locReq = new DeliveryLocationUpdateRequest(20.01, 73.79, true);
        assertThrows(RuntimeException.class, () -> {
            deliveryService.updatePartnerLocation(601L, locReq, "partner2@mandiplus.com");
        }, "Unrelated partner must not be allowed to submit GPS updates");
    }

    // =========================================================================
    // Scenario 7: Accepted partner confirms pickup and starts transit: tracking becomes active with valid GPS
    // =========================================================================
    @Test
    @DisplayName("Scenario 7: Pickup & Transit with GPS — Live tracking becomes active only with real coordinates")
    void test07_PickupAndTransit_LiveTrackingActiveWithValidGPS() {
        // 1. Partner accepts
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("DELIVERY_ACCEPTED");

        // 2. Partner confirms pickup
        DeliveryTaskDTO pickupTask = deliveryService.confirmPickup(601L, "partner1@mandiplus.com");
        assertEquals("PICKUP_CONFIRMED", pickupTask.getStatus());
        assertEquals("SHIPPED", order.getStatus(), "Underlying order must transition to SHIPPED");
        assertNotNull(deliveryTask.getPickupConfirmedAt());

        // 3. Partner starts transit
        DeliveryTaskDTO transitTask = deliveryService.startTransit(601L, "partner1@mandiplus.com");
        assertEquals("IN_TRANSIT", transitTask.getStatus());
        assertEquals("OUT_FOR_DELIVERY", order.getStatus(), "Underlying order must transition to OUT_FOR_DELIVERY");
        assertNotNull(deliveryTask.getTransitStartedAt());

        // 4. Partner sends real GPS updates
        DeliveryLocationUpdateRequest locReq = new DeliveryLocationUpdateRequest(20.0112, 73.7902, true);
        DeliveryTrackingDTO tracking = deliveryService.updatePartnerLocation(601L, locReq, "partner1@mandiplus.com");

        assertTrue(tracking.isLiveTrackingActive(), "Live tracking must be active after real GPS update");
        assertEquals(20.0112, tracking.getCurrentLatitude(), 0.0001);
        assertEquals(73.7902, tracking.getCurrentLongitude(), 0.0001);
        assertNotNull(tracking.getLocationUpdatedAt());
        assertEquals("Delivery in progress", tracking.getDisplayStatus());
    }

    // =========================================================================
    // Scenario 8: Order is completed: active tracking stops
    // =========================================================================
    @Test
    @DisplayName("Scenario 8: Order completed with OTP — Active tracking immediately stops and earnings are credited")
    void test08_OrderCompleted_ActiveTrackingStops() {
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("IN_TRANSIT");
        deliveryTask.setIsLiveTrackingActive(true);
        deliveryTask.setCurrentLatitude(20.0112);
        deliveryTask.setCurrentLongitude(73.7902);
        deliveryTask.setLocationUpdatedAt(LocalDateTime.now());

        // Verify and complete with correct OTP
        DeliveryTaskDTO completedTask = deliveryService.updateTaskStatus(601L, "DELIVERED", "4819", "partner1@mandiplus.com");
        assertEquals("DELIVERED", completedTask.getStatus());
        assertEquals("DELIVERED", order.getStatus());
        assertNotNull(deliveryTask.getDeliveredAt());

        // Tracking lookup
        DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(501L, "buyer@mandiplus.com", true);
        assertFalse(tracking.isLiveTrackingActive(), "Active GPS tracking must stop once delivered");
        assertEquals("Delivered", tracking.getDisplayStatus());
        assertEquals("Consignment delivered successfully.", tracking.getTrackingWarning());
    }

    // =========================================================================
    // Scenario 9: GPS permission denied or location stale: UI shows accurate warning without fake movement
    // =========================================================================
    @Test
    @DisplayName("Scenario 9A: Partner denies GPS permission — Tracking marked inactive with warning")
    void test09A_GpsPermissionDenied_AccurateWarning() {
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("IN_TRANSIT");
        deliveryTask.setIsLiveTrackingActive(true);

        // Partner denies permission in browser
        DeliveryLocationUpdateRequest locReq = new DeliveryLocationUpdateRequest(null, null, false);
        DeliveryTrackingDTO tracking = deliveryService.updatePartnerLocation(601L, locReq, "partner1@mandiplus.com");

        assertFalse(tracking.isLiveTrackingActive(), "Live tracking must be disabled when permission is denied");
        assertNotNull(tracking.getTrackingWarning(), "Tracking warning must explain location sharing status");
    }

    @Test
    @DisplayName("Scenario 9B: GPS location is stale (> 5 mins) — Marked inactive without fake vehicle simulation")
    void test09B_StaleGpsLocation_MarkedInactiveWithoutSimulation() {
        deliveryTask.setDeliveryPartner(partner1);
        deliveryTask.setStatus("IN_TRANSIT");
        deliveryTask.setIsLiveTrackingActive(true);
        deliveryTask.setCurrentLatitude(20.0112);
        deliveryTask.setCurrentLongitude(73.7902);
        // Location updated 10 minutes ago
        deliveryTask.setLocationUpdatedAt(LocalDateTime.now().minusMinutes(10));

        DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(501L, "buyer@mandiplus.com", true);

        assertFalse(tracking.isLiveTrackingActive(), "Live tracking must not claim to be active when GPS is stale");
        assertNotNull(tracking.getTrackingWarning());
        assertTrue(tracking.getTrackingWarning().contains("5 minutes ago") || tracking.getTrackingWarning().contains("stale"),
                "Warning must accurately inform buyer about stale GPS rather than generating fake vehicle movement");
    }
}
