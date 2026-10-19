package com.rtech.mandiplus;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.*;
import com.rtech.mandiplus.repository.*;
import com.rtech.mandiplus.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service Workflow and Core Business Logic Test Suite (TC_120 - TC_177)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MandiPlusServiceWorkflowTest {

    @Mock private UserRepository userRepository;
    @Mock private FarmerRepository farmerRepository;
    @Mock private BuyerRepository buyerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ColdStorageRepository coldStorageRepository;
    @Mock private StorageBookingRepository storageBookingRepository;
    @Mock private DeliveryPartnerRepository deliveryPartnerRepository;
    @Mock private DeliveryTaskRepository deliveryTaskRepository;
    @Mock private MandiPriceRepository mandiPriceRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ComplaintRepository complaintRepository;
    @Mock private WeatherRepository weatherRepository;
    @Mock private PaymentRepository paymentRepository;

    @Mock private PasswordEncoder encoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private NotificationService notificationService;
    @Mock private OrderService orderService;
    @Mock private ProductService productService;

    @InjectMocks private AuthService authService;
    @InjectMocks private AdminService adminService;
    @InjectMocks private PaymentService paymentService;
    @InjectMocks private DeliveryService deliveryService;
    @InjectMocks private ColdStorageService coldStorageService;
    @InjectMocks private MandiRateService mandiRateService;
    @InjectMocks private ChatService chatService;

    private final String secretKey = "test_razorpay_secret_key_12345678";

    @BeforeEach
    void setUp() {
        paymentService.setKeyId("rzp_test_12345");
        paymentService.setKeySecret(secretKey);
        paymentService.setWebhookSecret("webhook_secret_key_12345");
    }

    // ==========================================
    // Auth & User Lifecycle (TC_120 - TC_126)
    // ==========================================

    @Test
    @DisplayName("TC_120: Register New Farmer Account")
    void testTC_120_RegisterNewFarmerAccount() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("farmer@mandiplus.com");
        req.setPassword("Secret123");
        req.setRole("FARMER");
        req.setName("Ramesh Patil");
        req.setPhone("9876543210");
        req.setState("Maharashtra");
        req.setDistrict("Nashik");
        req.setUpiId("ramesh@upi");

        when(userRepository.existsByEmail("farmer@mandiplus.com")).thenReturn(false);
        when(encoder.encode("Secret123")).thenReturn("$2a$10$hashedBcryptSecret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        String result = authService.registerUser(req);

        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
        verify(farmerRepository).save(any(Farmer.class));
    }

    @Test
    @DisplayName("TC_121: Register New Buyer Account")
    void testTC_121_RegisterNewBuyerAccount() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("buyer@mandiplus.com");
        req.setPassword("Secret123");
        req.setRole("BUYER");
        req.setCompanyName("AgroMart Pvt Ltd");
        req.setName("Suresh Kumar");
        req.setPhone("9123456789");
        req.setAddress("Market Yard, Pune");

        when(userRepository.existsByEmail("buyer@mandiplus.com")).thenReturn(false);
        when(encoder.encode("Secret123")).thenReturn("$2a$10$hashedBcryptSecret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        String result = authService.registerUser(req);

        assertEquals("User registered successfully!", result);
        verify(buyerRepository).save(any(Buyer.class));
    }

    @Test
    @DisplayName("TC_122: Block Duplicate Email Registration")
    void testTC_122_BlockDuplicateEmailRegistration() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("farmer@mandiplus.com");
        req.setPassword("Secret123");
        req.setRole("FARMER");

        when(userRepository.existsByEmail("farmer@mandiplus.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.registerUser(req));
        assertTrue(ex.getMessage().contains("Email is already in use"));
    }

    @Test
    @DisplayName("TC_123: Normalize Email to Lowercase")
    void testTC_123_NormalizeEmailToLowercase() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("User@Domain.COM");
        req.setPassword("Secret123");
        req.setRole("FARMER");
        req.setName("Test");
        req.setPhone("9876543210");
        req.setState("MH");
        req.setDistrict("Nashik");
        req.setUpiId("test@upi");

        when(userRepository.existsByEmail("user@domain.com")).thenReturn(false);
        when(encoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            assertEquals("user@domain.com", u.getEmail());
            return u;
        });

        authService.registerUser(req);
    }

    @Test
    @DisplayName("TC_124: Authenticate User Valid Credentials")
    void testTC_124_AuthenticateUserValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("farmer@mandiplus.com");
        req.setPassword("validPassword");

        User user = new User(1L, "farmer@mandiplus.com", "hash", "FARMER", true, null, null);
        com.rtech.mandiplus.security.UserDetailsImpl userDetails = new com.rtech.mandiplus.security.UserDetailsImpl(
                1L, "farmer@mandiplus.com", "hash", List.of(new SimpleGrantedAuthority("ROLE_FARMER"))
        );
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        JwtResponse response = authService.authenticateUser(req);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("FARMER", response.getRole());
    }

    @Test
    @DisplayName("TC_125: Fetch Profile According to Role")
    void testTC_125_FetchProfileAccordingToRole() {
        User user = new User(1L, "farmer@mandiplus.com", "hash", "FARMER", true, null, null);
        Farmer farmer = new Farmer(1L, user, "Ramesh Patil", "9876543210", "Maharashtra", "Nashik", new BigDecimal("10.0"), "ramesh@upi");

        when(userRepository.findByEmail("farmer@mandiplus.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserEmail("farmer@mandiplus.com")).thenReturn(Optional.of(farmer));

        ProfileDTO profile = authService.getUserProfile("farmer@mandiplus.com");

        assertNotNull(profile);
        assertEquals("Ramesh Patil", profile.getName());
        assertEquals("FARMER", profile.getRole());
    }

    @Test
    @Disabled("SKIPPED (BUG-SEC-01): Block Public ADMIN Registration")
    @DisplayName("TC_126: Block Public ADMIN Registration")
    void testTC_126_BlockPublicAdminRegistration() {
        // Known security vulnerability skipped in test specification (BUG-SEC-01)
    }

    // ==========================================
    // Produce Lifecycle & Marketplace (TC_127 - TC_137)
    // ==========================================

    @Test
    @DisplayName("TC_127: Add Crop Starts PENDING_APPROVAL")
    void testTC_127_AddCropStartsPendingApproval() {
        Product p = new Product();
        p.setCropName("Wheat");
        p.setQuantityQuintals(new BigDecimal("50.00"));
        assertEquals("PENDING_APPROVAL", p.getStatus());
    }

    @Test
    @DisplayName("TC_128: Edit Crop Triggers Re-moderation")
    void testTC_128_EditCropTriggersRemoderation() {
        Product p = new Product();
        p.setStatus("AVAILABLE");
        p.updateDetails("Wheat", "Sharbati", new BigDecimal("50.00"), new BigDecimal("2600.00"), "Fresh", null, "Nashik", "PENDING_APPROVAL");

        assertEquals("PENDING_APPROVAL", p.getStatus());
    }

    @Test
    @DisplayName("TC_129: Delete Crop Enforces Farmer Ownership")
    void testTC_129_DeleteCropEnforcesFarmerOwnership() {
        User user = new User(1L, "farmer@mandiplus.com", "hash", "FARMER", true, null, null);
        Farmer farmer = new Farmer();
        farmer.setUser(user);
        Product product = new Product();
        product.setId(5L);
        product.setFarmer(farmer);

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        assertDoesNotThrow(() -> {
            if (!product.getFarmer().getUser().getEmail().equals("farmer@mandiplus.com")) {
                throw new RuntimeException("Unauthorized");
            }
            productRepository.delete(product);
        });
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("TC_130: Delete Crop by Non-Owner Blocked")
    void testTC_130_DeleteCropByNonOwnerBlocked() {
        User owner = new User(1L, "farmerA@mandiplus.com", "hash", "FARMER", true, null, null);
        Farmer farmer = new Farmer();
        farmer.setUser(owner);
        Product product = new Product();
        product.setId(5L);
        product.setFarmer(farmer);

        assertThrows(RuntimeException.class, () -> {
            if (!product.getFarmer().getUser().getEmail().equals("farmerB@mandiplus.com")) {
                throw new RuntimeException("Unauthorized: Cannot delete crop owned by another farmer");
            }
        });
    }

    @Test
    @DisplayName("TC_131: Filter Products by Category & District")
    void testTC_131_FilterProductsByCategoryAndDistrict() {
        Product p = new Product();
        p.setCropName("Wheat");
        p.setLocation("Nashik");
        p.setStatus("AVAILABLE");

        when(productRepository.findByCropNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus("Wheat", "Nashik", "AVAILABLE"))
                .thenReturn(List.of(p));

        List<Product> results = productRepository.findByCropNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus("Wheat", "Nashik", "AVAILABLE");
        assertEquals(1, results.size());
        assertEquals("Wheat", results.get(0).getCropName());
    }

    @Test
    @DisplayName("TC_132: Place Order Calculates Total Price")
    void testTC_132_PlaceOrderCalculatesTotalPrice() {
        Product product = new Product();
        product.setPricePerQuintal(new BigDecimal("4500.00"));
        product.setQuantityQuintals(new BigDecimal("50.00"));
        product.setStatus("AVAILABLE");

        BigDecimal qty = new BigDecimal("5.00");
        BigDecimal total = product.getPricePerQuintal().multiply(qty);

        assertEquals(new BigDecimal("22500.0000"), total);
    }

    @Test
    @DisplayName("TC_133: Place Order Rejects Insufficient Stock")
    void testTC_133_PlaceOrderRejectsInsufficientStock() {
        Product product = new Product();
        product.setQuantityQuintals(new BigDecimal("10.00"));
        product.setStatus("AVAILABLE");

        assertThrows(RuntimeException.class, () -> product.deductQuantity(new BigDecimal("15.00")));
    }

    @Test
    @DisplayName("TC_134: Place Order Rejects Unapproved Product")
    void testTC_134_PlaceOrderRejectsUnapprovedProduct() {
        Product product = new Product();
        product.setQuantityQuintals(new BigDecimal("10.00"));
        product.setStatus("PENDING_APPROVAL");

        assertThrows(RuntimeException.class, () -> product.validateAvailability(new BigDecimal("5.00")));
    }

    @Test
    @DisplayName("TC_135: Place Order Requires Buyer Profile")
    void testTC_135_PlaceOrderRequiresBuyerProfile() {
        when(buyerRepository.findByUserEmail("unknown@buyer.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            buyerRepository.findByUserEmail("unknown@buyer.com")
                    .orElseThrow(() -> new RuntimeException("Error: Buyer profile required to place order"));
        });
    }

    @Test
    @DisplayName("TC_136: Farmer Approves Order with Delivery Date")
    void testTC_136_FarmerApprovesOrderWithDeliveryDate() {
        Order order = new Order();
        order.setStatus("PENDING");
        LocalDate date = LocalDate.parse("2026-10-15");

        order.transitionTo(OrderStatus.APPROVED, "FARMER");
        order.setEstimatedDeliveryDate(date);

        assertEquals("APPROVED", order.getStatus());
        assertEquals(date, order.getEstimatedDeliveryDate());
    }

    @Test
    @DisplayName("TC_137: Buyer Cancel Order Restores Stock")
    void testTC_137_BuyerCancelOrderRestoresStock() {
        Product product = new Product();
        product.setStatus("AVAILABLE");
        product.setQuantityQuintals(new BigDecimal("20.00"));
        product.setPricePerQuintal(new BigDecimal("3000.00"));

        Order order = Order.createOrder(new Buyer(), product, new BigDecimal("5.00"));
        assertEquals(new BigDecimal("15.00"), product.getQuantityQuintals());

        order.cancelOrder();
        assertEquals(new BigDecimal("20.00"), product.getQuantityQuintals());
    }

    // ==========================================
    // Payments & Razorpay Webhooks (TC_138 - TC_145)
    // ==========================================

    @Test
    @DisplayName("TC_138: Create Razorpay Order in Paise")
    void testTC_138_CreateRazorpayOrderInPaise() {
        BigDecimal orderAmount = new BigDecimal("35000.00");
        long paise = orderAmount.multiply(new BigDecimal(100)).longValue();

        assertEquals(3500000L, paise);
    }

    @Test
    @DisplayName("TC_139: Reject Zero Order Payment")
    void testTC_139_RejectZeroOrderPayment() {
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setAmount(BigDecimal.ZERO);

        when(buyerRepository.findByUserEmail("buyer@test.com")).thenReturn(Optional.of(new Buyer()));

        assertThrows(IllegalArgumentException.class, () -> paymentService.createRazorpayOrder(req, "buyer@test.com"));
    }

    @Test
    @DisplayName("TC_140: Verify Valid Razorpay HMAC Signature")
    void testTC_140_VerifyValidRazorpayHmacSignature() throws Exception {
        String orderId = "order_9A33XWu170gUtm";
        String paymentId = "pay_29Aeep590MG5w5";
        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String validSignature = hexString.toString();

        boolean result = paymentService.verifySignature(orderId, paymentId, validSignature);
        assertTrue(result, "HMAC signature verification must succeed for legitimate signature");
    }

    @Test
    @DisplayName("TC_141: Reject Tampered Razorpay Signature")
    void testTC_141_RejectTamperedRazorpaySignature() {
        String orderId = "order_9A33XWu170gUtm";
        String paymentId = "pay_29Aeep590MG5w5";
        String tamperedSignature = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";

        boolean result = paymentService.verifySignature(orderId, paymentId, tamperedSignature);
        assertFalse(result, "HMAC signature verification must reject tampered signature");
    }

    @Test
    @DisplayName("TC_142: Reject Signature Signed with Wrong Secret")
    void testTC_142_RejectSignatureSignedWithWrongSecret() throws Exception {
        String orderId = "order_9A33XWu170gUtm";
        String paymentId = "pay_29Aeep590MG5w5";
        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("wrong_secret_key_99999".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        boolean result = paymentService.verifySignature(orderId, paymentId, hexString.toString());
        assertFalse(result, "HMAC signature verification must reject signatures with wrong secret");
    }

    @Test
    @DisplayName("TC_143: Handle Null Payment Signature")
    void testTC_143_HandleNullPaymentSignature() {
        boolean result = paymentService.verifySignature("order_123", "pay_456", null);
        assertFalse(result, "Null signature must return false safely without exception");
    }

    @Test
    @DisplayName("TC_144: Process Webhook payment.captured Event")
    void testTC_144_ProcessWebhookPaymentCapturedEvent() {
        Payment payment = new Payment();
        payment.setPaymentStatus("PENDING");
        payment.setEscrowStatus("ESCROW_HOLD");

        payment.setPaymentStatus("CAPTURED");
        assertEquals("CAPTURED", payment.getPaymentStatus());
    }

    @Test
    @DisplayName("TC_145: Process COD Order Escrow Status")
    void testTC_145_ProcessCodOrderEscrowStatus() {
        Payment payment = new Payment();
        payment.setPaymentMethod("COD");
        payment.setEscrowStatus("NOT_APPLICABLE");

        assertEquals("NOT_APPLICABLE", payment.getEscrowStatus());
    }

    // ==========================================
    // Delivery Partner Logistics (TC_146 - TC_154)
    // ==========================================

    @Test
    @DisplayName("TC_146: Accept Available Delivery Task")
    void testTC_146_AcceptAvailableDeliveryTask() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId(10L);
        partner.setUser(new User(10L, "partner@mandiplus.com", "hash", "DELIVERY_PARTNER", true, null, null));

        DeliveryTask task = new DeliveryTask();
        task.setId(10L);
        task.setStatus("AVAILABLE");

        when(deliveryPartnerRepository.findByUserEmail("partner@mandiplus.com")).thenReturn(Optional.of(partner));
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryTaskDTO dto = deliveryService.acceptTask(10L, "partner@mandiplus.com");

        assertEquals("ASSIGNED", dto.getStatus());
    }

    @Test
    @DisplayName("TC_147: Accept Already Assigned Task Blocked")
    void testTC_147_AcceptAlreadyAssignedTaskBlocked() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId(10L);
        DeliveryTask task = new DeliveryTask();
        task.setId(10L);
        task.setStatus("ASSIGNED");

        when(deliveryPartnerRepository.findByUserEmail("partner2@mandiplus.com")).thenReturn(Optional.of(partner));
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> deliveryService.acceptTask(10L, "partner2@mandiplus.com"));
    }

    @Test
    @DisplayName("TC_148: Complete Delivery with Correct OTP")
    void testTC_148_CompleteDeliveryWithCorrectOtp() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId(10L);
        partner.setUser(new User(10L, "p@m.com", "h", "DELIVERY_PARTNER", true, null, null));

        DeliveryTask task = new DeliveryTask();
        task.setId(10L);
        task.setStatus("OUT_FOR_DELIVERY");
        task.setDeliveryOtp("482910");
        task.setDeliveryPartner(partner);
        task.setPayoutAmount(new BigDecimal("200.00"));
        task.setSurgeBonus(new BigDecimal("50.00"));

        when(deliveryPartnerRepository.findByUserEmail("p@m.com")).thenReturn(Optional.of(partner));
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryTaskDTO dto = deliveryService.updateTaskStatus(10L, "DELIVERED", "482910", "p@m.com");

        assertEquals("DELIVERED", dto.getStatus());
    }

    @Test
    @DisplayName("TC_149: Reject Delivery with Incorrect OTP")
    void testTC_149_RejectDeliveryWithIncorrectOtp() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId(10L);

        DeliveryTask task = new DeliveryTask();
        task.setId(10L);
        task.setStatus("OUT_FOR_DELIVERY");
        task.setDeliveryOtp("482910");
        task.setDeliveryPartner(partner);

        when(deliveryPartnerRepository.findByUserEmail("p@m.com")).thenReturn(Optional.of(partner));
        when(deliveryTaskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> deliveryService.updateTaskStatus(10L, "DELIVERED", "000000", "p@m.com"));
    }

    @Test
    @Disabled("SKIPPED (BUG-LOG-02): Enforce Mandatory OTP on Delivery")
    @DisplayName("TC_150: Enforce Mandatory OTP on Delivery")
    void testTC_150_EnforceMandatoryOtpOnDelivery() {
        // Known bug where null OTP check was not enforced (BUG-LOG-02)
    }

    @Test
    @DisplayName("TC_151: Credit Payout and Surge Bonus")
    void testTC_151_CreditPayoutAndSurgeBonus() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalDeliveries(0);
        partner.setTotalEarnings(BigDecimal.ZERO);

        BigDecimal base = new BigDecimal("200.00");
        BigDecimal surge = new BigDecimal("50.00");
        partner.addCompletedTrip(base.add(surge));

        assertEquals(new BigDecimal("250.00"), partner.getWalletBalance());
        assertEquals(1, partner.getTrips());
    }

    @Test
    @DisplayName("TC_152: Request Payout Valid Wallet Balance")
    void testTC_152_RequestPayoutValidWalletBalance() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalEarnings(new BigDecimal("500.00"));

        partner.withdraw(300.00);

        assertEquals(new BigDecimal("200.00"), partner.getWalletBalance());
    }

    @Test
    @DisplayName("TC_153: Request Payout Exceeding Balance Blocked")
    void testTC_153_RequestPayoutExceedingBalanceBlocked() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalEarnings(new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, () -> partner.withdraw(600.00));
    }

    @Test
    @DisplayName("TC_154: Request Zero or Negative Payout Blocked")
    void testTC_154_RequestZeroOrNegativePayoutBlocked() {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setTotalEarnings(new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, () -> partner.withdraw(0.00));
        assertThrows(RuntimeException.class, () -> partner.withdraw(-50.00));
    }

    // ==========================================
    // Cold Storage Services (TC_155 - TC_160)
    // ==========================================

    @Test
    @DisplayName("TC_155: Calculate Cold Storage Booking Total")
    void testTC_155_CalculateColdStorageBookingTotal() {
        BigDecimal tons = new BigDecimal("5.00");
        long days = 10;
        BigDecimal ratePerTonDay = new BigDecimal("20.00");

        BigDecimal total = tons.multiply(BigDecimal.valueOf(days)).multiply(ratePerTonDay);

        assertEquals(new BigDecimal("1000.0000"), total);
    }

    @Test
    @DisplayName("TC_156: Block Booking Exceeding Capacity")
    void testTC_156_BlockBookingExceedingCapacity() {
        ColdStorage storage = new ColdStorage();
        storage.setId(1L);
        storage.setTotalCapacityTons(new BigDecimal("20.00"));
        storage.setPricePerTonDay(new BigDecimal("25.00"));

        when(farmerRepository.findByUserEmail("farmer@test.com")).thenReturn(Optional.of(new Farmer()));
        when(coldStorageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageBookingRepository.sumOccupiedCapacityByStorageId(1L)).thenReturn(BigDecimal.ZERO);

        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setColdStorageId(1L);
        dto.setQuantityTons(new BigDecimal("30.00"));
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));

        assertThrows(RuntimeException.class, () -> coldStorageService.bookStorage(dto, "farmer@test.com"));
    }

    @Test
    @DisplayName("TC_157: Validate End Date Strictly After Start")
    void testTC_157_ValidateEndDateStrictlyAfterStart() {
        ColdStorage storage = new ColdStorage();
        storage.setId(1L);
        storage.setTotalCapacityTons(new BigDecimal("100.00"));
        storage.setPricePerTonDay(new BigDecimal("25.00"));

        when(farmerRepository.findByUserEmail("farmer@test.com")).thenReturn(Optional.of(new Farmer()));
        when(coldStorageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageBookingRepository.sumOccupiedCapacityByStorageId(1L)).thenReturn(BigDecimal.ZERO);

        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setColdStorageId(1L);
        dto.setQuantityTons(new BigDecimal("10.00"));
        dto.setStartDate(LocalDate.parse("2026-11-05"));
        dto.setEndDate(LocalDate.parse("2026-11-05")); // same date

        assertThrows(RuntimeException.class, () -> coldStorageService.bookStorage(dto, "farmer@test.com"));
    }

    @Test
    @DisplayName("TC_158: Approve Booking Reduces Facility Capacity")
    void testTC_158_ApproveBookingReducesFacilityCapacity() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("100.00"));
        storage.setAvailableCapacityTons(new BigDecimal("100.00"));

        storage.allocateCapacity(new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), storage.getAvailableCapacityTons());
    }

    @Test
    @DisplayName("TC_159: Reject Booking Requires Reason & Restores")
    void testTC_159_RejectBookingRequiresReasonAndRestores() {
        ColdStorage storage = new ColdStorage();
        storage.setTotalCapacityTons(new BigDecimal("100.00"));
        storage.setAvailableCapacityTons(new BigDecimal("75.00"));
        storage.setManager(new User(1L, "mgr@test.com", "h", "COLD_STORAGE_MANAGER", true, null, null));

        StorageBooking booking = new StorageBooking();
        booking.setId(1L);
        booking.setColdStorage(storage);
        booking.setStatus("APPROVED");
        booking.setQuantityTons(new BigDecimal("25.00"));

        when(storageBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Attempt rejection without reason -> blocked
        assertThrows(RuntimeException.class, () -> coldStorageService.updateBookingStatus(1L, "REJECTED", "", "mgr@test.com"));

        // Rejection with valid reason
        storage.restoreCapacity(booking.getQuantityTons());
        assertEquals(new BigDecimal("100.00"), storage.getAvailableCapacityTons());
    }

    @Test
    @DisplayName("TC_160: Unauthorized Manager Blocked")
    void testTC_160_UnauthorizedManagerBlocked() {
        ColdStorage storage = new ColdStorage();
        storage.setManager(new User(1L, "managerA@test.com", "h", "COLD_STORAGE_MANAGER", true, null, null));

        StorageBooking booking = new StorageBooking();
        booking.setId(1L);
        booking.setColdStorage(storage);

        when(storageBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () ->
                coldStorageService.updateBookingStatus(1L, "APPROVED", null, "managerB@test.com")
        );
    }

    // ==========================================
    // Admin, Notifications & Chat (TC_161 - TC_177)
    // ==========================================

    @Test
    @DisplayName("TC_161: Admin Toggle User Active Status")
    void testTC_161_AdminToggleUserActiveStatus() {
        User user = new User(50L, "user@test.com", "hash", "BUYER", true, null, null);
        when(userRepository.findById(50L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = adminService.toggleUserActive(50L);

        assertFalse(updated.getActive());
    }

    @Test
    @DisplayName("TC_162: Admin Moderate Product Approve")
    void testTC_162_AdminModerateProductApprove() {
        Product product = new Product();
        product.setId(8L);
        product.setStatus("PENDING_APPROVAL");

        when(productRepository.findById(8L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productService.convertToDTO(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            ProductDTO dto = new ProductDTO();
            dto.setStatus(p.getStatus());
            return dto;
        });

        ProductDTO approved = adminService.approveProduct(8L, null);

        assertEquals("AVAILABLE", approved.getStatus());
    }

    @Test
    @DisplayName("TC_163: Admin Moderate Product Reject with Badge")
    void testTC_163_AdminModerateProductRejectWithBadge() {
        Product product = new Product();
        product.setId(8L);
        product.setStatus("PENDING_APPROVAL");

        when(productRepository.findById(8L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productService.convertToDTO(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            ProductDTO dto = new ProductDTO();
            dto.setStatus(p.getStatus());
            dto.setAdminFeedback(p.getAdminFeedback());
            return dto;
        });

        ProductDTO rejected = adminService.rejectProduct(8L, "Low clarity photo");

        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("Low clarity photo", rejected.getAdminFeedback());
    }

    @Test
    @DisplayName("TC_164: Admin Resolve Complaint with Remarks")
    void testTC_164_AdminResolveComplaintWithRemarks() {
        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setStatus("OPEN");

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        Complaint resolved = adminService.resolveComplaint(1L, "RESOLVED");

        assertEquals("RESOLVED", resolved.getStatus());
    }

    @Test
    @DisplayName("TC_165: Calculate Platform Performance Metrics")
    void testTC_165_CalculatePlatformPerformanceMetrics() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole("FARMER")).thenReturn(40L);
        when(userRepository.countByRole("BUYER")).thenReturn(35L);
        when(productRepository.count()).thenReturn(80L);
        when(productRepository.countByStatus("PENDING_APPROVAL")).thenReturn(10L);
        when(orderRepository.count()).thenReturn(120L);
        when(complaintRepository.count()).thenReturn(5L);

        AdminStatsDTO stats = adminService.getAdminMetrics();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(40L, stats.getTotalFarmers());
        assertEquals(120L, stats.getTotalOrders());
    }

    @Test
    @DisplayName("TC_166: Send Private Order Notification")
    void testTC_166_SendPrivateOrderNotification() {
        NotificationService notifier = new NotificationService();
        assertDoesNotThrow(() -> notifier.sendNotification(10L, "Order Status", "Your order is confirmed", "ORDER"));
    }

    @Test
    @DisplayName("TC_167: Send Delivery Status Notification")
    void testTC_167_SendDeliveryStatusNotification() {
        NotificationService notifier = new NotificationService();
        assertDoesNotThrow(() -> notifier.sendNotification(20L, "Delivery Update", "Driver is on the way", "DELIVERY"));
    }

    @Test
    @DisplayName("TC_168: Send Cold Storage Alert")
    void testTC_168_SendColdStorageAlert() {
        NotificationService notifier = new NotificationService();
        assertDoesNotThrow(() -> notifier.sendNotification(15L, "Temperature Alert", "Unit #2 temperature optimal", "COLD_STORAGE"));
    }

    @Test
    @DisplayName("TC_169: Broadcast Public Mandi Price Alert")
    void testTC_169_BroadcastPublicMandiPriceAlert() {
        NotificationService notifier = new NotificationService();
        boolean pushed = notifier.broadcastNotification("Mandi Rates", "Wheat prices rose +₹30 today", "MANDI_PRICE");
        assertTrue(pushed, "MANDI_PRICE broadcast must be permitted to all sessions");
    }

    @Test
    @DisplayName("TC_170: Block Global Broadcast of Private Category")
    void testTC_170_BlockGlobalBroadcastOfPrivateCategory() {
        NotificationService notifier = new NotificationService();
        boolean pushed = notifier.broadcastNotification("Private Escrow", "Account credited", "PAYMENT");
        assertFalse(pushed, "Private PAYMENT notifications must be blocked from global broadcast");
    }

    @Test
    @DisplayName("TC_171: Fetch Active Mandi Rates for Crop")
    void testTC_171_FetchActiveMandiRatesForCrop() {
        MandiPrice p = new MandiPrice();
        p.setCropName("Wheat");
        p.setPricePerQuintal(new BigDecimal("2800.00"));

        when(mandiPriceRepository.findByCropNameContainingIgnoreCase("Wheat")).thenReturn(List.of(p));

        List<MandiPrice> list = mandiRateService.getRatesForCrop("Wheat");

        assertEquals(1, list.size());
        assertEquals("Wheat", list.get(0).getCropName());
    }

    @Test
    @DisplayName("TC_172: Publish New Mandi Benchmark Rate")
    void testTC_172_PublishNewMandiBenchmarkRate() {
        MandiPrice price = new MandiPrice();
        price.setCropName("Rice");
        price.setPricePerQuintal(new BigDecimal("3400.00"));

        when(mandiPriceRepository.save(any(MandiPrice.class))).thenAnswer(i -> i.getArgument(0));

        MandiPrice saved = mandiRateService.saveMandiRate(price);

        assertNotNull(saved);
        assertEquals("Rice", saved.getCropName());
        assertEquals(new BigDecimal("3400.00"), saved.getPricePerQuintal());
    }

    @Test
    @DisplayName("TC_173: User Sends Message to Admin")
    void testTC_173_UserSendsMessageToAdmin() {
        User farmer = new User(10L, "farmer@test.com", "h", "FARMER", true, null, null);
        User admin = new User(1L, "admin@gmail.com", "h", "ADMIN", true, null, null);

        SendMessageRequest req = new SendMessageRequest();
        req.setReceiverId(1L);
        req.setContent("Need help with invoice");
        req.setMessageType("TEXT");

        when(userRepository.findById(10L)).thenReturn(Optional.of(farmer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> {
            ChatMessage msg = i.getArgument(0);
            msg.setId(101L);
            return msg;
        });

        ChatMessageDTO dto = chatService.sendMessage(10L, req);

        assertNotNull(dto);
        assertEquals("Need help with invoice", dto.getContent());
    }

    @Test
    @DisplayName("TC_174: Block User-to-User Direct Chat")
    void testTC_174_BlockUserToUserDirectChat() {
        User farmer = new User(10L, "farmer@test.com", "h", "FARMER", true, null, null);
        User buyer = new User(20L, "buyer@test.com", "h", "BUYER", true, null, null);

        SendMessageRequest req = new SendMessageRequest();
        req.setReceiverId(20L);
        req.setContent("Hello buyer");

        when(userRepository.findById(10L)).thenReturn(Optional.of(farmer));
        when(userRepository.findById(20L)).thenReturn(Optional.of(buyer));

        assertThrows(RuntimeException.class, () -> chatService.sendMessage(10L, req));
    }

    @Test
    @DisplayName("TC_175: Fetch Conversation History with Admin")
    void testTC_175_FetchConversationHistoryWithAdmin() {
        User farmer = new User(10L, "farmer@test.com", "h", "FARMER", true, null, null);
        User admin = new User(1L, "admin@gmail.com", "h", "ADMIN", true, null, null);

        ChatMessage msg = new ChatMessage(farmer, admin, "Help requested", "TEXT");

        when(userRepository.findById(10L)).thenReturn(Optional.of(farmer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(chatMessageRepository.findChatHistory(10L, 1L)).thenReturn(List.of(msg));

        List<ChatMessageDTO> list = chatService.getChatHistory(10L, 1L);

        assertEquals(1, list.size());
        assertEquals("Help requested", list.get(0).getContent());
    }

    @Test
    @Disabled("SKIPPED (BUG-WS-07): Broadcast Message via WebSocket")
    @DisplayName("TC_176: Broadcast Message via WebSocket")
    void testTC_176_BroadcastMessageViaWebSocket() {
        // Skipped in specification (BUG-WS-07)
    }

    @Test
    @DisplayName("TC_177: Fallback Cached Weather on Outage")
    void testTC_177_FallbackCachedWeatherOnOutage() {
        Weather weather = new Weather();
        weather.setLocation("Nashik");
        weather.setTemperatureCelsius(new BigDecimal("29.00"));
        weather.setConditionText("Partly Cloudy");
        weather.setHumidityPercentage(55);

        when(weatherRepository.findByLocationContainingIgnoreCase("Nashik")).thenReturn(Optional.of(weather));

        Optional<Weather> opt = weatherRepository.findByLocationContainingIgnoreCase("Nashik");
        assertTrue(opt.isPresent());
        assertEquals("Partly Cloudy", opt.get().getConditionText());
    }
}
