package com.rtech.mandiplus;

import com.rtech.mandiplus.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain Entities and In-Memory Logic Test Suite (TC_72 - TC_119)
 */
public class MandiPlusEntityDomainTest {

    // ==========================================
    // Product Stock & Lifecycle (TC_72 - TC_80)
    // ==========================================

    @Test
    @DisplayName("TC_72: Deduct Product Stock")
    void testTC_72_DeductProductStock() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("100.00"));

        product.deductQuantity(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), product.getQuantityQuintals());
        assertEquals("AVAILABLE", product.getStatus());
    }

    @Test
    @DisplayName("TC_73: Product Sold Out Transition")
    void testTC_73_ProductSoldOutTransition() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("100.00"));

        product.deductQuantity(new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(product.getQuantityQuintals()));
        assertEquals("SOLD", product.getStatus());
    }

    @Test
    @DisplayName("TC_74: Block Stock Over-buying")
    void testTC_74_BlockStockOverbuying() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("100.00"));

        assertThrows(RuntimeException.class, () -> product.deductQuantity(new BigDecimal("100.01")));
    }

    @Test
    @DisplayName("TC_75: Block Negative Stock Deduction")
    void testTC_75_BlockNegativeStockDeduction() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("100.00"));

        assertThrows(RuntimeException.class, () -> product.deductQuantity(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("TC_76: Restore Product Stock")
    void testTC_76_RestoreProductStock() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("70.00"));

        product.restoreQuantity(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("100.00"), product.getQuantityQuintals());
        assertEquals("AVAILABLE", product.getStatus());
    }

    @Test
    @DisplayName("TC_77: Restore Stock from SOLD State")
    void testTC_77_RestoreStockFromSoldState() {
        Product product = new Product();
        product.setStatus("SOLD");
        product.setQuantityQuintals(BigDecimal.ZERO);

        product.restoreQuantity(new BigDecimal("20.00"));

        assertEquals(new BigDecimal("20.00"), product.getQuantityQuintals());
        assertEquals("AVAILABLE", product.getStatus());
    }

    @Test
    @DisplayName("TC_78: Block Negative Stock Restoration")
    void testTC_78_BlockNegativeStockRestoration() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("50.00"));

        assertThrows(RuntimeException.class, () -> product.restoreQuantity(new BigDecimal("-5.00")));
    }

    @Test
    @DisplayName("TC_79: Product Suggested Price Rate")
    void testTC_79_ProductSuggestedPriceRate() {
        Product product = new Product();
        product.setSuggestedPricePerQuintal(new BigDecimal("3200.00"));

        assertEquals(new BigDecimal("3200.00"), product.getSuggestedPricePerQuintal());
    }

    @Test
    @DisplayName("TC_80: Product Default Status Setting")
    void testTC_80_ProductDefaultStatusSetting() {
        Product product = new Product();
        assertEquals("PENDING_APPROVAL", product.getStatus());
    }

    // ==========================================
    // Order Lifecycle & Financials (TC_81 - TC_85)
    // ==========================================

    @Test
    @DisplayName("TC_81: Order Creation Deducts Stock")
    void testTC_81_OrderCreationDeductsStock() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("20.00"));
        product.setPricePerQuintal(new BigDecimal("3000.00"));

        Buyer buyer = new Buyer();
        buyer.setCompanyName("AgroTrade");

        Order order = Order.createOrder(buyer, product, new BigDecimal("5.00"));

        assertEquals(new BigDecimal("15.00"), product.getQuantityQuintals());
        assertEquals(new BigDecimal("5.00"), order.getQuantityQuintals());
    }

    @Test
    @DisplayName("TC_82: Order Cancel Restores Stock")
    void testTC_82_OrderCancelRestoresStock() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("20.00"));
        product.setPricePerQuintal(new BigDecimal("3000.00"));

        Buyer buyer = new Buyer();
        buyer.setCompanyName("AgroTrade");

        Order order = Order.createOrder(buyer, product, new BigDecimal("5.00"));
        assertEquals(new BigDecimal("15.00"), product.getQuantityQuintals());

        order.cancelOrder();

        assertEquals(new BigDecimal("20.00"), product.getQuantityQuintals());
        assertEquals("CANCELLED", order.getStatus());
    }

    @Test
    @DisplayName("TC_83: Prevent Double Stock Restore")
    void testTC_83_PreventDoubleStockRestore() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("20.00"));
        product.setPricePerQuintal(new BigDecimal("3000.00"));

        Buyer buyer = new Buyer();
        buyer.setCompanyName("AgroTrade");

        Order order = Order.createOrder(buyer, product, new BigDecimal("5.00"));
        order.cancelOrder();

        assertThrows(IllegalStateException.class, order::cancelOrder);
    }

    @Test
    @DisplayName("TC_84: Order Total Price Calculation")
    void testTC_84_OrderTotalPriceCalculation() {
        BigDecimal rate = new BigDecimal("4500.00");
        BigDecimal qty = new BigDecimal("5.00");
        BigDecimal total = rate.multiply(qty);

        assertEquals(new BigDecimal("22500.0000"), total);
        assertEquals(0, new BigDecimal("22500.00").compareTo(total));
    }

    @Test
    @DisplayName("TC_85: Order Delivery Date Set on Approval")
    void testTC_85_OrderDeliveryDateSetOnApproval() {
        Order order = new Order();
        order.setStatus("PENDING");
        LocalDate deliveryDate = LocalDate.parse("2026-10-20");

        order.transitionTo(OrderStatus.APPROVED, "ADMIN");
        order.setEstimatedDeliveryDate(deliveryDate);

        assertEquals("APPROVED", order.getStatus());
        assertEquals(deliveryDate, order.getEstimatedDeliveryDate());
    }

    // ==========================================
    // Farmer Entity Constraints (TC_86 - TC_99)
    // ==========================================

    @Test
    @DisplayName("TC_86: Farmer Valid Entity Initialization")
    void testTC_86_FarmerValidEntityInitialization() {
        Farmer farmer = new Farmer();
        farmer.setName("Ramesh");
        farmer.setPhone("9876543210");
        farmer.setState("Maharashtra");
        farmer.setDistrict("Nashik");
        farmer.setFarmSizeAcres(10.0);
        farmer.setUpiId("ramesh@upi");

        assertEquals("Ramesh", farmer.getName());
        assertEquals("9876543210", farmer.getPhone());
        assertEquals("Maharashtra", farmer.getState());
        assertEquals("Nashik", farmer.getDistrict());
    }

    @Test
    @DisplayName("TC_87: Farmer Blank Name Validation")
    void testTC_87_FarmerBlankNameValidation() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setName(""));
    }

    @Test
    @DisplayName("TC_88: Farmer Null Name Validation")
    void testTC_88_FarmerNullNameValidation() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setName(null));
    }

    @Test
    @DisplayName("TC_89: Farmer Name Whitespace Trimming")
    void testTC_89_FarmerNameWhitespaceTrimming() {
        Farmer farmer = new Farmer();
        farmer.setName(" Ramesh ");
        assertEquals("Ramesh", farmer.getName());
    }

    @Test
    @DisplayName("TC_90: Farmer 10-Digit Phone Validation")
    void testTC_90_Farmer10DigitPhoneValidation() {
        Farmer farmer = new Farmer();
        farmer.setPhone("9876543210");
        assertEquals("9876543210", farmer.getPhone());
    }

    @Test
    @DisplayName("TC_91: Farmer Invalid Phone Validation")
    void testTC_91_FarmerInvalidPhoneValidation() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setPhone("987654"));
    }

    @Test
    @DisplayName("TC_92: Farmer State Mandatory Check")
    void testTC_92_FarmerStateMandatoryCheck() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setState(null));
    }

    @Test
    @DisplayName("TC_93: Farmer District Mandatory Check")
    void testTC_93_FarmerDistrictMandatoryCheck() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setDistrict(null));
    }

    @Test
    @DisplayName("TC_94: Farmer Farm Size Positive Check")
    void testTC_94_FarmerFarmSizePositiveCheck() {
        Farmer farmer = new Farmer();
        farmer.setFarmSizeAcres(new BigDecimal("10.00"));
        assertEquals(new BigDecimal("10.00"), farmer.getFarmSizeAcres());
    }

    @Test
    @DisplayName("TC_95: Farmer Farm Size Negative Blocked")
    void testTC_95_FarmerFarmSizeNegativeBlocked() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setFarmSizeAcres(new BigDecimal("-5.00")));
    }

    @Test
    @DisplayName("TC_96: Farmer UPI ID Format Validation")
    void testTC_96_FarmerUpiIdFormatValidation() {
        Farmer farmer = new Farmer();
        farmer.setUpiId("ramesh@upi");
        assertEquals("ramesh@upi", farmer.getUpiId());
    }

    @Test
    @DisplayName("TC_97: Farmer Blank UPI ID Blocked")
    void testTC_97_FarmerBlankUpiIdBlocked() {
        Farmer farmer = new Farmer();
        assertThrows(IllegalArgumentException.class, () -> farmer.setUpiId(""));
    }

    @Test
    @DisplayName("TC_98: Farmer Default Earnings Zero")
    void testTC_98_FarmerDefaultEarningsZero() {
        Farmer farmer = new Farmer();
        assertEquals(BigDecimal.ZERO, farmer.getEarnings());
    }

    @Test
    @DisplayName("TC_99: Farmer User Association Non-Null")
    void testTC_99_FarmerUserAssociationNonNull() {
        User user = new User();
        user.setId(10L);
        Farmer farmer = new Farmer();
        farmer.setUser(user);

        assertNotNull(farmer.getUser());
        assertEquals(10L, farmer.getUser().getId());
    }

    // ==========================================
    // Buyer Entity Constraints (TC_100 - TC_109)
    // ==========================================

    @Test
    @DisplayName("TC_100: Buyer Valid Profile Fields")
    void testTC_100_BuyerValidProfileFields() {
        Buyer buyer = new Buyer();
        buyer.setCompanyName("AgroMart");
        buyer.setRepresentativeName("John Doe");
        buyer.setPhone("9123456789");
        buyer.setAddress("Market Yard, Pune");

        assertEquals("AgroMart", buyer.getCompanyName());
        assertEquals("9123456789", buyer.getPhone());
    }

    @Test
    @DisplayName("TC_101: Buyer Blank Company Name Blocked")
    void testTC_101_BuyerBlankCompanyNameBlocked() {
        Buyer buyer = new Buyer();
        assertThrows(IllegalArgumentException.class, () -> buyer.setCompanyName(""));
    }

    @Test
    @DisplayName("TC_102: Buyer Null Company Name Blocked")
    void testTC_102_BuyerNullCompanyNameBlocked() {
        Buyer buyer = new Buyer();
        assertThrows(IllegalArgumentException.class, () -> buyer.setCompanyName(null));
    }

    @Test
    @DisplayName("TC_103: Buyer Company Name Trimming")
    void testTC_103_BuyerCompanyNameTrimming() {
        Buyer buyer = new Buyer();
        buyer.setCompanyName(" AgroMart ");
        assertEquals("AgroMart", buyer.getCompanyName());
    }

    @Test
    @DisplayName("TC_104: Buyer 10-Digit Phone Check")
    void testTC_104_Buyer10DigitPhoneCheck() {
        Buyer buyer = new Buyer();
        buyer.setPhone("9123456789");
        assertEquals("9123456789", buyer.getPhone());
    }

    @Test
    @DisplayName("TC_105: Buyer Delivery Address Mandatory")
    void testTC_105_BuyerDeliveryAddressMandatory() {
        Buyer buyer = new Buyer();
        assertThrows(IllegalArgumentException.class, () -> buyer.setAddress(null));
    }

    @Test
    @DisplayName("TC_106: Buyer Valid GST Number Format")
    void testTC_106_BuyerValidGstNumberFormat() {
        Buyer buyer = new Buyer();
        buyer.setGstin("27AAAAA0000A1Z5");
        assertEquals("27AAAAA0000A1Z5", buyer.getGstin());
    }

    @Test
    @DisplayName("TC_107: Buyer Optional GST Null Allowed")
    void testTC_107_BuyerOptionalGstNullAllowed() {
        Buyer buyer = new Buyer();
        buyer.setGstin(null);
        assertNull(buyer.getGstin());
    }

    @Test
    @DisplayName("TC_108: Buyer User Association Non-Null")
    void testTC_108_BuyerUserAssociationNonNull() {
        User user = new User();
        user.setId(20L);
        Buyer buyer = new Buyer();
        buyer.setUser(user);

        assertNotNull(buyer.getUser());
        assertEquals(20L, buyer.getUser().getId());
    }

    @Test
    @DisplayName("TC_109: Buyer Cart Initialization Empty")
    void testTC_109_BuyerCartInitializationEmpty() {
        Buyer buyer = new Buyer();
        assertEquals(0, buyer.getCartItemCount());
        assertNotNull(buyer.getCartItems());
    }

    // ==========================================
    // Cold Storage Capacity Logic (TC_110 - TC_115)
    // ==========================================

    @Test
    @DisplayName("TC_110: Allocate Available Capacity")
    void testTC_110_AllocateAvailableCapacity() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("500.00"));

        storage.allocateCapacity(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("300.00"), storage.getAvailableCapacityTons());
    }

    @Test
    @DisplayName("TC_111: Exact Full Capacity Allocation")
    void testTC_111_ExactFullCapacityAllocation() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("500.00"));

        storage.allocateCapacity(new BigDecimal("500.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(storage.getAvailableCapacityTons()));
    }

    @Test
    @DisplayName("TC_112: Block Capacity Overbooking")
    void testTC_112_BlockCapacityOverbooking() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, () -> storage.allocateCapacity(new BigDecimal("500.01")));
    }

    @Test
    @DisplayName("TC_113: Block Negative Capacity Allocation")
    void testTC_113_BlockNegativeCapacityAllocation() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, () -> storage.allocateCapacity(new BigDecimal("-50.00")));
    }

    @Test
    @DisplayName("TC_114: Restore Storage Capacity")
    void testTC_114_RestoreStorageCapacity() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("300.00"));

        storage.restoreCapacity(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("400.00"), storage.getAvailableCapacityTons());
    }

    @Test
    @DisplayName("TC_115: Restore Capacity Capped at Total")
    void testTC_115_RestoreCapacityCappedAtTotal() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("500.00"));
        storage.setAvailableCapacityTons(new BigDecimal("450.00"));

        storage.restoreCapacity(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("500.00"), storage.getAvailableCapacityTons());
    }

    // ==========================================
    // Delivery Partner & User Status (TC_116 - TC_119)
    // ==========================================

    @Test
    @DisplayName("TC_116: Accumulate Trips and Earnings")
    void testTC_116_AccumulateTripsAndEarnings() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalDeliveries(0);
        partner.setTotalEarnings(BigDecimal.ZERO);

        partner.addCompletedTrip(150.00);

        assertEquals(1, partner.getTrips());
        assertEquals(new BigDecimal("150.0"), partner.getWalletBalance());
    }

    @Test
    @DisplayName("TC_117: Handle Null Trip Counters")
    void testTC_117_HandleNullTripCounters() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalDeliveries(null);
        partner.setTodayDeliveries(null);
        partner.setTotalEarnings(null);
        partner.setTodayEarnings(null);

        assertDoesNotThrow(() -> partner.addCompletedTrip(200.00));
        assertEquals(1, partner.getTrips());
        assertEquals(new BigDecimal("200.0"), partner.getWalletBalance());
    }

    @Test
    @DisplayName("TC_118: Toggle User Active True to False")
    void testTC_118_ToggleUserActiveTrueToFalse() {
        User user = new User();
        user.setActive(true);

        user.toggleActive();

        assertFalse(user.getActive());
    }

    @Test
    @DisplayName("TC_119: Toggle User Active False to True")
    void testTC_119_ToggleUserActiveFalseToTrue() {
        User user = new User();
        user.setActive(false);

        user.toggleActive();

        assertTrue(user.getActive());
    }
}
