package com.rtech.mandiplus;

import com.rtech.mandiplus.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite for Order State Machine Transitions Across Roles (TC_11 - TC_71)
 */
public class MandiPlusStateTransitionTest {

    // ==========================================
    // FARMER Transitions (TC_11 - TC_21)
    // ==========================================

    @Test
    @DisplayName("TC_11: State Transition: FARMER (PENDING -> APPROVED)")
    void testTC_11_FarmerPendingToApproved() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.APPROVED, "FARMER"));
    }

    @Test
    @DisplayName("TC_12: State Transition: FARMER (PENDING -> REJECTED)")
    void testTC_12_FarmerPendingToRejected() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.REJECTED, "FARMER"));
    }

    @Test
    @DisplayName("TC_13: State Transition: FARMER (PENDING -> SHIPPED)")
    void testTC_13_FarmerPendingToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.SHIPPED, "FARMER"));
    }

    @Test
    @DisplayName("TC_14: State Transition: FARMER (PENDING -> DELIVERED)")
    void testTC_14_FarmerPendingToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.DELIVERED, "FARMER"));
    }

    @Test
    @DisplayName("TC_15: State Transition: FARMER (PENDING -> CANCELLED)")
    void testTC_15_FarmerPendingToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED, "FARMER"));
    }

    @Test
    @DisplayName("TC_16: State Transition: FARMER (APPROVED -> SHIPPED)")
    void testTC_16_FarmerApprovedToShipped() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.SHIPPED, "FARMER"));
    }

    @Test
    @DisplayName("TC_17: State Transition: FARMER (APPROVED -> PENDING)")
    void testTC_17_FarmerApprovedToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.PENDING, "FARMER"));
    }

    @Test
    @DisplayName("TC_18: State Transition: FARMER (APPROVED -> DELIVERED)")
    void testTC_18_FarmerApprovedToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.DELIVERED, "FARMER"));
    }

    @Test
    @DisplayName("TC_19: State Transition: FARMER (APPROVED -> CANCELLED)")
    void testTC_19_FarmerApprovedToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.CANCELLED, "FARMER"));
    }

    @Test
    @DisplayName("TC_20: State Transition: FARMER (SHIPPED -> DELIVERED)")
    void testTC_20_FarmerShippedToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED, "FARMER"));
    }

    @Test
    @DisplayName("TC_21: State Transition: FARMER (SHIPPED -> CANCELLED)")
    void testTC_21_FarmerShippedToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED, "FARMER"));
    }

    // ==========================================
    // BUYER Transitions (TC_22 - TC_30)
    // ==========================================

    @Test
    @DisplayName("TC_22: State Transition: BUYER (PENDING -> CANCELLED)")
    void testTC_22_BuyerPendingToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED, "BUYER"));
    }

    @Test
    @DisplayName("TC_23: State Transition: BUYER (PENDING -> APPROVED)")
    void testTC_23_BuyerPendingToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.APPROVED, "BUYER"));
    }

    @Test
    @DisplayName("TC_24: State Transition: BUYER (PENDING -> REJECTED)")
    void testTC_24_BuyerPendingToRejected() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.REJECTED, "BUYER"));
    }

    @Test
    @DisplayName("TC_25: State Transition: BUYER (PENDING -> SHIPPED)")
    void testTC_25_BuyerPendingToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.SHIPPED, "BUYER"));
    }

    @Test
    @DisplayName("TC_26: State Transition: BUYER (PENDING -> DELIVERED)")
    void testTC_26_BuyerPendingToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.DELIVERED, "BUYER"));
    }

    @Test
    @DisplayName("TC_27: State Transition: BUYER (APPROVED -> CANCELLED)")
    void testTC_27_BuyerApprovedToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.CANCELLED, "BUYER"));
    }

    @Test
    @DisplayName("TC_28: State Transition: BUYER (SHIPPED -> CANCELLED)")
    void testTC_28_BuyerShippedToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED, "BUYER"));
    }

    @Test
    @DisplayName("TC_29: State Transition: BUYER (OUT_FOR_DELIVERY -> CANCELLED)")
    void testTC_29_BuyerOutForDeliveryToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, "BUYER"));
    }

    @Test
    @DisplayName("TC_30: State Transition: BUYER (DELIVERED -> CANCELLED)")
    void testTC_30_BuyerDeliveredToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED, "BUYER"));
    }

    // ==========================================
    // DELIVERY_PARTNER Transitions (TC_31 - TC_38)
    // ==========================================

    @Test
    @DisplayName("TC_31: State Transition: DELIVERY_PARTNER (APPROVED -> SHIPPED)")
    void testTC_31_DeliveryPartnerApprovedToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.SHIPPED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_32: State Transition: DELIVERY_PARTNER (SHIPPED -> OUT_FOR_DELIVERY)")
    void testTC_32_DeliveryPartnerShippedToOutForDelivery() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_33: State Transition: DELIVERY_PARTNER (SHIPPED -> DELIVERED)")
    void testTC_33_DeliveryPartnerShippedToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_34: State Transition: DELIVERY_PARTNER (SHIPPED -> CANCELLED)")
    void testTC_34_DeliveryPartnerShippedToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_35: State Transition: DELIVERY_PARTNER (OUT_FOR_DELIVERY -> DELIVERED)")
    void testTC_35_DeliveryPartnerOutForDeliveryToDelivered() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_36: State Transition: DELIVERY_PARTNER (OUT_FOR_DELIVERY -> CANCELLED)")
    void testTC_36_DeliveryPartnerOutForDeliveryToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_37: State Transition: DELIVERY_PARTNER (PENDING -> APPROVED)")
    void testTC_37_DeliveryPartnerPendingToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.APPROVED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_38: State Transition: DELIVERY_PARTNER (PENDING -> OUT_FOR_DELIVERY)")
    void testTC_38_DeliveryPartnerPendingToOutForDelivery() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.OUT_FOR_DELIVERY, "DELIVERY_PARTNER"));
    }

    // ==========================================
    // COLD_STORAGE_MANAGER Transitions (TC_39 - TC_41)
    // ==========================================

    @Test
    @DisplayName("TC_39: State Transition: COLD_STORAGE_MANAGER (PENDING -> APPROVED)")
    void testTC_39_ColdStorageManagerPendingToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.APPROVED, "COLD_STORAGE_MANAGER"));
    }

    @Test
    @DisplayName("TC_40: State Transition: COLD_STORAGE_MANAGER (PENDING -> CANCELLED)")
    void testTC_40_ColdStorageManagerPendingToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED, "COLD_STORAGE_MANAGER"));
    }

    @Test
    @DisplayName("TC_41: State Transition: COLD_STORAGE_MANAGER (SHIPPED -> DELIVERED)")
    void testTC_41_ColdStorageManagerShippedToDelivered() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED, "COLD_STORAGE_MANAGER"));
    }

    // ==========================================
    // ADMIN Transitions (TC_42 - TC_51)
    // ==========================================

    @Test
    @DisplayName("TC_42: State Transition: ADMIN (PENDING -> CANCELLED)")
    void testTC_42_AdminPendingToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_43: State Transition: ADMIN (APPROVED -> CANCELLED)")
    void testTC_43_AdminApprovedToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_44: State Transition: ADMIN (SHIPPED -> CANCELLED)")
    void testTC_44_AdminShippedToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_45: State Transition: ADMIN (OUT_FOR_DELIVERY -> CANCELLED)")
    void testTC_45_AdminOutForDeliveryToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_46: State Transition: ADMIN (PENDING -> APPROVED)")
    void testTC_46_AdminPendingToApproved() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.APPROVED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_47: State Transition: ADMIN (PENDING -> REJECTED)")
    void testTC_47_AdminPendingToRejected() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.REJECTED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_48: State Transition: ADMIN (DELIVERED -> PENDING)")
    void testTC_48_AdminDeliveredToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.PENDING, "ADMIN"));
    }

    @Test
    @DisplayName("TC_49: State Transition: ADMIN (DELIVERED -> CANCELLED)")
    void testTC_49_AdminDeliveredToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_50: State Transition: ADMIN (CANCELLED -> PENDING)")
    void testTC_50_AdminCancelledToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING, "ADMIN"));
    }

    @Test
    @DisplayName("TC_51: State Transition: ADMIN (REJECTED -> APPROVED)")
    void testTC_51_AdminRejectedToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.APPROVED, "ADMIN"));
    }

    // ==========================================
    // Terminal State Immutability (TC_52 - TC_66)
    // ==========================================

    @Test
    @DisplayName("TC_52: State Transition: FARMER (DELIVERED -> CANCELLED)")
    void testTC_52_FarmerDeliveredToCancelled() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED, "FARMER"));
    }

    @Test
    @DisplayName("TC_53: State Transition: BUYER (DELIVERED -> PENDING)")
    void testTC_53_BuyerDeliveredToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.PENDING, "BUYER"));
    }

    @Test
    @DisplayName("TC_54: State Transition: DELIVERY_PARTNER (DELIVERED -> SHIPPED)")
    void testTC_54_DeliveryPartnerDeliveredToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.SHIPPED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_55: State Transition: COLD_STORAGE_MANAGER (DELIVERED -> PENDING)")
    void testTC_55_ColdStorageManagerDeliveredToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.PENDING, "COLD_STORAGE_MANAGER"));
    }

    @Test
    @DisplayName("TC_56: State Transition: ADMIN (DELIVERED -> SHIPPED)")
    void testTC_56_AdminDeliveredToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.DELIVERED, OrderStatus.SHIPPED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_57: State Transition: FARMER (CANCELLED -> APPROVED)")
    void testTC_57_FarmerCancelledToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.APPROVED, "FARMER"));
    }

    @Test
    @DisplayName("TC_58: State Transition: BUYER (CANCELLED -> PENDING)")
    void testTC_58_BuyerCancelledToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING, "BUYER"));
    }

    @Test
    @DisplayName("TC_59: State Transition: DELIVERY_PARTNER (CANCELLED -> SHIPPED)")
    void testTC_59_DeliveryPartnerCancelledToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.SHIPPED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_60: State Transition: COLD_STORAGE_MANAGER (CANCELLED -> APPROVED)")
    void testTC_60_ColdStorageManagerCancelledToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.APPROVED, "COLD_STORAGE_MANAGER"));
    }

    @Test
    @DisplayName("TC_61: State Transition: ADMIN (CANCELLED -> APPROVED)")
    void testTC_61_AdminCancelledToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.APPROVED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_62: State Transition: FARMER (REJECTED -> PENDING)")
    void testTC_62_FarmerRejectedToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.PENDING, "FARMER"));
    }

    @Test
    @DisplayName("TC_63: State Transition: BUYER (REJECTED -> PENDING)")
    void testTC_63_BuyerRejectedToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.PENDING, "BUYER"));
    }

    @Test
    @DisplayName("TC_64: State Transition: DELIVERY_PARTNER (REJECTED -> SHIPPED)")
    void testTC_64_DeliveryPartnerRejectedToShipped() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.SHIPPED, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_65: State Transition: COLD_STORAGE_MANAGER (REJECTED -> APPROVED)")
    void testTC_65_ColdStorageManagerRejectedToApproved() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.APPROVED, "COLD_STORAGE_MANAGER"));
    }

    @Test
    @DisplayName("TC_66: State Transition: ADMIN (REJECTED -> PENDING)")
    void testTC_66_AdminRejectedToPending() {
        assertFalse(OrderStatus.isValidTransition(OrderStatus.REJECTED, OrderStatus.PENDING, "ADMIN"));
    }

    // ==========================================
    // Identity Transitions (current == target) (TC_67 - TC_71)
    // ==========================================

    @Test
    @DisplayName("TC_67: State Transition: FARMER (APPROVED -> APPROVED)")
    void testTC_67_FarmerApprovedToApproved() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.APPROVED, "FARMER"));
    }

    @Test
    @DisplayName("TC_68: State Transition: BUYER (PENDING -> PENDING)")
    void testTC_68_BuyerPendingToPending() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.PENDING, OrderStatus.PENDING, "BUYER"));
    }

    @Test
    @DisplayName("TC_69: State Transition: DELIVERY_PARTNER (OUT_FOR_DELIVERY -> OUT_FOR_DELIVERY)")
    void testTC_69_DeliveryPartnerOutForDeliveryToOutForDelivery() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY, "DELIVERY_PARTNER"));
    }

    @Test
    @DisplayName("TC_70: State Transition: ADMIN (CANCELLED -> CANCELLED)")
    void testTC_70_AdminCancelledToCancelled() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.CANCELLED, OrderStatus.CANCELLED, "ADMIN"));
    }

    @Test
    @DisplayName("TC_71: State Transition: COLD_STORAGE_MANAGER (APPROVED -> APPROVED)")
    void testTC_71_ColdStorageManagerApprovedToApproved() {
        assertTrue(OrderStatus.isValidTransition(OrderStatus.APPROVED, OrderStatus.APPROVED, "COLD_STORAGE_MANAGER"));
    }
}
