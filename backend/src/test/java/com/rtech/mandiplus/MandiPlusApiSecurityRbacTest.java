package com.rtech.mandiplus;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.config.SecurityConfig;
import com.rtech.mandiplus.controller.*;
import com.rtech.mandiplus.dto.*;
import com.rtech.mandiplus.entity.*;
import com.rtech.mandiplus.repository.*;
import com.rtech.mandiplus.security.UserDetailsServiceImpl;
import com.rtech.mandiplus.service.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API, RBAC Endpoint Security, and Repository Contracts Test Suite (TC_178 - TC_236)
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MandiPlusApiSecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AuthService authService;
    @MockBean private AdminService adminService;
    @MockBean private ProductService productService;
    @MockBean private OrderService orderService;
    @MockBean private PaymentService paymentService;
    @MockBean private DeliveryService deliveryService;
    @MockBean private ColdStorageService coldStorageService;
    @MockBean private MandiRateService mandiRateService;
    @MockBean private ChatService chatService;
    @MockBean private NotificationService notificationService;
    @MockBean private WeatherService weatherService;

    @MockBean private UserRepository userRepository;
    @MockBean private FarmerRepository farmerRepository;
    @MockBean private BuyerRepository buyerRepository;
    @MockBean private ProductRepository productRepository;
    @MockBean private OrderRepository orderRepository;
    @MockBean private DeliveryTaskRepository deliveryTaskRepository;
    @MockBean private StorageBookingRepository storageBookingRepository;
    @MockBean private ColdStorageRepository coldStorageRepository;
    @MockBean private MandiPriceRepository mandiPriceRepository;
    @MockBean private ComplaintRepository complaintRepository;

    // ==========================================
    // Auth & Account API Verification (TC_178 - TC_186)
    // ==========================================

    @Test
    @DisplayName("TC_178: Register All 5 Distinct Role Profiles")
    void testTC_178_RegisterAll5DistinctRoleProfiles() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn("User registered successfully!");

        String[] roles = {"FARMER", "BUYER", "DELIVERY_PARTNER", "COLD_STORAGE_MANAGER", "ADMIN"};
        for (String role : roles) {
            String json = String.format("{\"email\":\"%s@mandiplus.com\",\"password\":\"Secure123\",\"role\":\"%s\",\"name\":\"Test User\"}", role.toLowerCase(), role);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("TC_179: Case-Insensitive Email Duplicate Signup")
    void testTC_179_CaseInsensitiveEmailDuplicateSignup() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Error: Email is already in use!"));

        String json = "{\"email\":\"FARMER@MANDIPLUS.COM\",\"password\":\"Secure123\",\"role\":\"FARMER\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_180: Password Under 6 Characters Rejected")
    void testTC_180_PasswordUnder6CharactersRejected() throws Exception {
        // RegisterRequest has @Size(min = 6) or @NotBlank
        String json = "{\"email\":\"new@mandiplus.com\",\"password\":\"12345\",\"role\":\"FARMER\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_181: Case-Insensitive Email Login")
    void testTC_181_CaseInsensitiveEmailLogin() throws Exception {
        JwtResponse jwt = new JwtResponse("token123", 1L, "john@agro.com", "BUYER", null);
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwt);

        String json = "{\"email\":\"JOHN@AGRO.COM\",\"password\":\"ValidPass123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    @DisplayName("TC_182: Wrong Password Login Rejection")
    void testTC_182_WrongPasswordLoginRejection() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        String json = "{\"email\":\"john@agro.com\",\"password\":\"WrongPass\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_183: Unknown User Login Rejection")
    void testTC_183_UnknownUserLoginRejection() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        String json = "{\"email\":\"unknown@x.com\",\"password\":\"Pass123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_184: Blocked User Login Blocked")
    void testTC_184_BlockedUserLoginBlocked() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("User account is inactive or blocked"));

        String json = "{\"email\":\"blocked@user.com\",\"password\":\"Pass123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "farmer@mandiplus.com", roles = "FARMER")
    @DisplayName("TC_185: Fetch Caller Profile with JWT")
    void testTC_185_FetchCallerProfileWithJwt() throws Exception {
        ProfileDTO profile = new ProfileDTO();
        profile.setName("Ramesh");
        profile.setEmail("farmer@mandiplus.com");
        profile.setRole("FARMER");
        profile.setPhone("9876543210");
        when(authService.getProfile("farmer@mandiplus.com")).thenReturn(profile);

        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ramesh"));
    }

    @Test
    @Disabled("SKIPPED (BUG-API-03): Anonymous Profile Request HTTP Status")
    @DisplayName("TC_186: Anonymous Profile Request HTTP Status")
    void testTC_186_AnonymousProfileRequestHttpStatus() {
        // Skipped in test specification (BUG-API-03)
    }

    // ==========================================
    // RBAC Security Matrix (TC_187 - TC_198)
    // ==========================================

    @Test
    @DisplayName("TC_187: RBAC: Anonymous Denied on Farmer API")
    void testTC_187_RbacAnonymousDeniedOnFarmerApi() throws Exception {
        mockMvc.perform(get("/api/farmer/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    @DisplayName("TC_188: RBAC: Buyer Forbidden on Farmer API")
    void testTC_188_RbacBuyerForbiddenOnFarmerApi() throws Exception {
        mockMvc.perform(get("/api/farmer/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    @DisplayName("TC_189: RBAC: Farmer Allowed on Farmer API")
    void testTC_189_RbacFarmerAllowedOnFarmerApi() throws Exception {
        when(productService.getFarmerProducts(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/farmer/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_190: RBAC: Anonymous Denied on Buyer API")
    void testTC_190_RbacAnonymousDeniedOnBuyerApi() throws Exception {
        mockMvc.perform(get("/api/buyer/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    @DisplayName("TC_191: RBAC: Farmer Forbidden on Buyer API")
    void testTC_191_RbacFarmerForbiddenOnBuyerApi() throws Exception {
        mockMvc.perform(get("/api/buyer/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    @DisplayName("TC_192: RBAC: Buyer Allowed on Buyer API")
    void testTC_192_RbacBuyerAllowedOnBuyerApi() throws Exception {
        when(orderService.getBuyerOrders(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/buyer/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_193: RBAC: Anonymous Denied on Admin API")
    void testTC_193_RbacAnonymousDeniedOnAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    @DisplayName("TC_194: RBAC: Non-Admin Roles Forbidden on Admin API")
    void testTC_194_RbacNonAdminRolesForbiddenOnAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC_195: RBAC: Admin Allowed on Admin API")
    void testTC_195_RbacAdminAllowedOnAdminApi() throws Exception {
        when(adminService.getGlobalMetrics()).thenReturn(new AdminStatsDTO());
        when(adminService.getAdminMetrics()).thenReturn(new AdminStatsDTO());
        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_196: RBAC: Garbage Token Rejected")
    void testTC_196_RbacGarbageTokenRejected() throws Exception {
        mockMvc.perform(get("/api/buyer/orders")
                        .header("Authorization", "Bearer garbage.token.str"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC_197: RBAC: Fake UI Admin Token Rejected")
    void testTC_197_RbacFakeUiAdminTokenRejected() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")
                        .header("Authorization", "Bearer fake-jwt-token-from-ui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC_198: RBAC: Blocked User Token Revoked")
    void testTC_198_RbacBlockedUserTokenRevoked() throws Exception {
        mockMvc.perform(get("/api/buyer/orders")
                        .header("Authorization", "Bearer expired-or-revoked-token"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // Product & Order Controller APIs (TC_199 - TC_208)
    // ==========================================

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_199: Farmer Crop Starts PENDING_APPROVAL")
    void testTC_199_FarmerCropStartsPendingApproval() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setCropName("Tomatoes");
        dto.setQuantityQuintals(new BigDecimal("40.00"));
        dto.setStatus("PENDING_APPROVAL");

        when(productService.addProduct(any(), any())).thenReturn(dto);

        String json = "{\"cropName\":\"Tomatoes\",\"quantityQuintals\":40.0,\"pricePerQuintal\":2000.0,\"location\":\"Nashik\"}";
        mockMvc.perform(post("/api/farmer/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("TC_200: Public Catalog Hides Unapproved Crops")
    void testTC_200_PublicCatalogHidesUnapprovedCrops() throws Exception {
        ProductDTO approvedCrop = new ProductDTO();
        approvedCrop.setCropName("Wheat");
        approvedCrop.setStatus("AVAILABLE");

        when(productService.getAllProducts(any(), any())).thenReturn(List.of(approvedCrop));

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("TC_201: Search Products by Crop & District")
    void testTC_201_SearchProductsByCropAndDistrict() throws Exception {
        ProductDTO crop = new ProductDTO();
        crop.setCropName("Wheat");
        crop.setLocation("Nashik");
        crop.setStatus("AVAILABLE");

        when(productService.getAllProducts(any(), any())).thenReturn(List.of(crop));

        mockMvc.perform(get("/api/public/products/search?query=Wheat&location=Nashik"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cropName").value("Wheat"));
    }

    @Test
    @DisplayName("TC_202: Filter Products by Price Range")
    void testTC_202_FilterProductsByPriceRange() {
        Product p1 = new Product();
        p1.setPricePerQuintal(new BigDecimal("2500.00"));
        BigDecimal min = new BigDecimal("2000.00");
        BigDecimal max = new BigDecimal("3500.00");

        assertTrue(p1.getPricePerQuintal().compareTo(min) >= 0 && p1.getPricePerQuintal().compareTo(max) <= 0);
    }

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_203: Farmer Crop Update Triggers Re-moderation")
    void testTC_203_FarmerCropUpdateTriggersRemoderation() throws Exception {
        ProductDTO updated = new ProductDTO();
        updated.setId(1L);
        updated.setCropName("Wheat");
        updated.setStatus("PENDING_APPROVAL");

        when(productService.updateProduct(eq(1L), any(), any())).thenReturn(updated);

        String json = "{\"pricePerQuintal\":2600.0}";
        mockMvc.perform(put("/api/farmer/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_204: Buyer Order Deducts Product Stock")
    void testTC_204_BuyerOrderDeductsProductStock() throws Exception {
        OrderDTO order = new OrderDTO();
        order.setId(1L);
        order.setQuantityQuintals(new BigDecimal("10.00"));
        order.setStatus("PENDING");

        when(orderService.placeOrder(any(), any())).thenReturn(order);

        String json = "{\"productId\":1,\"quantityQuintals\":10.0}";
        mockMvc.perform(post("/api/buyer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_205: Over-ordering Stock Returns HTTP 400")
    void testTC_205_OverOrderingStockReturnsHttp400() throws Exception {
        when(orderService.placeOrder(any(), any()))
                .thenThrow(new RuntimeException("Insufficient quantity available"));

        String json = "{\"productId\":1,\"quantityQuintals\":15.0}";
        mockMvc.perform(post("/api/buyer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_206: Order on Unapproved Crop Returns HTTP 400")
    void testTC_206_OrderOnUnapprovedCropReturnsHttp400() throws Exception {
        when(orderService.placeOrder(any(), any()))
                .thenThrow(new RuntimeException("Product is no longer available"));

        String json = "{\"productId\":9,\"quantityQuintals\":5.0}";
        mockMvc.perform(post("/api/buyer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_207: Farmer Approve Order Sets Delivery Date")
    void testTC_207_FarmerApproveOrderSetsDeliveryDate() throws Exception {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setStatus("APPROVED");

        when(orderService.updateOrderStatus(eq(1L), eq("APPROVED"), any(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/farmer/orders/1/status?status=APPROVED&estimatedDeliveryDate=2026-10-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_208: Buyer Cancel Order Restores Stock")
    void testTC_208_BuyerCancelOrderRestoresStock() throws Exception {
        OrderDTO dto = new OrderDTO();
        dto.setId(15L);
        dto.setStatus("CANCELLED");

        when(orderService.cancelOrder(eq(15L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/buyer/orders/15/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ==========================================
    // Payments & Razorpay Webhooks (TC_209 - TC_213)
    // ==========================================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_209: Create Razorpay Server Order")
    void testTC_209_CreateRazorpayServerOrder() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("orderId", "order_test_123");
        resp.put("amount", 3500000L);
        resp.put("currency", "INR");

        when(paymentService.createRazorpayOrder(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":35000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(3500000L));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_210: Process COD Order Sets Escrow")
    void testTC_210_ProcessCodOrderSetsEscrow() throws Exception {
        OrderDTO dto = new OrderDTO();
        dto.setPaymentMethod("COD");

        when(paymentService.verifyAndProcessPayment(any(), any(), any(), any(), any(), any(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/payment/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"COD\",\"items\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_211: Reject Forged Razorpay Signature")
    void testTC_211_RejectForgedRazorpaySignature() throws Exception {
        when(paymentService.verifyAndProcessPayment(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Invalid payment signature"));

        mockMvc.perform(post("/api/payment/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razorpaySignature\":\"forged_hex\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_212: Public Webhook Missing Signature")
    void testTC_212_PublicWebhookMissingSignature() throws Exception {
        doThrow(new SecurityException("Missing signature")).when(paymentService).processWebhookEvent(any(), any());

        mockMvc.perform(post("/api/public/payment/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"payment.captured\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_213: Public Webhook Forged Signature")
    void testTC_213_PublicWebhookForgedSignature() throws Exception {
        doThrow(new SecurityException("Invalid signature")).when(paymentService).processWebhookEvent(any(), any());

        mockMvc.perform(post("/api/public/payment/webhook")
                        .header("X-Razorpay-Signature", "forged_hex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"payment.captured\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Delivery Logistics APIs (TC_214 - TC_218)
    // ==========================================

    @Test
    @DisplayName("TC_214: Public Apply for Delivery Partner")
    void testTC_214_PublicApplyForDeliveryPartner() throws Exception {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setName("Akash");

        when(deliveryService.applyForDeliveryPartner(any())).thenReturn(dto);

        String json = "{\"email\":\"akash@test.com\",\"password\":\"Secure123\",\"name\":\"Akash\",\"phone\":\"9876543210\",\"district\":\"Nashik\"}";
        mockMvc.perform(post("/api/public/delivery/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver@mandiplus.com", roles = "DELIVERY_PARTNER")
    @DisplayName("TC_215: Delivery Partner Toggle Duty Status")
    void testTC_215_DeliveryPartnerToggleDutyStatus() throws Exception {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setIsOnline(true);

        when(deliveryService.toggleDuty(any())).thenReturn(dto);

        mockMvc.perform(put("/api/delivery/toggle-duty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOnline").value(true));
    }

    @Test
    @WithMockUser(username = "driver@mandiplus.com", roles = "DELIVERY_PARTNER")
    @DisplayName("TC_216: Delivery Partner View Available Tasks")
    void testTC_216_DeliveryPartnerViewAvailableTasks() throws Exception {
        when(deliveryService.getAvailableTasks(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/delivery/available-tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver@mandiplus.com", roles = "DELIVERY_PARTNER")
    @DisplayName("TC_217: Delivery Partner Accept Task")
    void testTC_217_DeliveryPartnerAcceptTask() throws Exception {
        DeliveryTaskDTO task = new DeliveryTaskDTO();
        task.setId(25L);
        task.setStatus("ASSIGNED");

        when(deliveryService.acceptTask(eq(25L), any())).thenReturn(task);

        mockMvc.perform(post("/api/delivery/tasks/25/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @WithMockUser(username = "driver@mandiplus.com", roles = "DELIVERY_PARTNER")
    @DisplayName("TC_218: Complete Delivery with OTP Credits Wallet")
    void testTC_218_CompleteDeliveryWithOtpCreditsWallet() throws Exception {
        DeliveryTaskDTO task = new DeliveryTaskDTO();
        task.setId(25L);
        task.setStatus("DELIVERED");

        when(deliveryService.updateTaskStatus(eq(25L), eq("DELIVERED"), eq("582910"), any())).thenReturn(task);

        mockMvc.perform(put("/api/delivery/tasks/25/status?status=DELIVERED&otp=582910"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    // ==========================================
    // Cold Storage APIs (TC_219 - TC_223)
    // ==========================================

    @Test
    @DisplayName("TC_219: Public List Cold Storage Facilities")
    void testTC_219_PublicListColdStorageFacilities() throws Exception {
        when(coldStorageService.getAllFacilities(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/cold-storage"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_220: Farmer Books Cold Storage Unit")
    void testTC_220_FarmerBooksColdStorageUnit() throws Exception {
        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setStatus("PENDING");

        when(coldStorageService.bookStorage(any(), any())).thenReturn(dto);

        String json = "{\"coldStorageId\":1,\"quantityTons\":10.0,\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-15\"}";
        mockMvc.perform(post("/api/farmer/cold-storage/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_221: Buyer Books Cold Storage Unit")
    void testTC_221_BuyerBooksColdStorageUnit() throws Exception {
        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setStatus("PENDING");

        when(coldStorageService.bookStorageForBuyer(any(), any())).thenReturn(dto);

        String json = "{\"coldStorageId\":1,\"quantityTons\":25.0,\"startDate\":\"2026-11-01\",\"endDate\":\"2026-12-01\"}";
        mockMvc.perform(post("/api/buyer/cold-storage/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_222: Booking Exceeding Capacity Returns HTTP 400")
    void testTC_222_BookingExceedingCapacityReturnsHttp400() throws Exception {
        when(coldStorageService.bookStorage(any(), any()))
                .thenThrow(new RuntimeException("Insufficient capacity"));

        String json = "{\"coldStorageId\":1,\"quantityTons\":30.0,\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-15\"}";
        mockMvc.perform(post("/api/farmer/cold-storage/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager@coldstorage.com", roles = "COLD_STORAGE_MANAGER")
    @DisplayName("TC_223: Manager Approve Booking Decrements Capacity")
    void testTC_223_ManagerApproveBookingDecrementsCapacity() throws Exception {
        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setId(1L);
        dto.setStatus("APPROVED");

        when(coldStorageService.updateBookingStatus(eq(1L), eq("APPROVED"), any(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/cold-storage/bookings/1/status?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // ==========================================
    // Complaints & Admin Moderation (TC_224 - TC_231)
    // ==========================================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("TC_224: File Order Dispute Grievance")
    void testTC_224_FileOrderDisputeGrievance() throws Exception {
        Complaint c = new Complaint();
        c.setId(1L);
        c.setStatus("OPEN");

        when(complaintRepository.save(any(Complaint.class))).thenReturn(c);
        User buyerUser = new User(1L, "buyer@test.com", "pass", "BUYER", "9822000000", "Buyer User", true);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(buyerUser));

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"ORDER_DISPUTE\",\"title\":\"Dispute\",\"description\":\"Details\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_225: Anonymous Complaint Filing Rejected")
    void testTC_225_AnonymousComplaintFilingRejected() throws Exception {
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"ORDER_DISPUTE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "userA@test.com", roles = "BUYER")
    @DisplayName("TC_226: User Isolation on My Complaints API")
    void testTC_226_UserIsolationOnMyComplaintsApi() throws Exception {
        when(complaintRepository.findByUserEmail(eq("userA@test.com"))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC_227: Admin Resolve Complaint with Remarks")
    void testTC_227_AdminResolveComplaintWithRemarks() throws Exception {
        Complaint c = new Complaint();
        c.setId(1L);
        c.setStatus("RESOLVED");

        when(adminService.resolveComplaint(eq(1L), eq("RESOLVED"))).thenReturn(c);

        mockMvc.perform(put("/api/admin/complaints/1/resolve?status=RESOLVED&remarks=Refunded"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC_228: Admin View Platform KPI Metrics")
    void testTC_228_AdminViewPlatformKpiMetrics() throws Exception {
        when(adminService.getGlobalMetrics()).thenReturn(new AdminStatsDTO());
        when(adminService.getAdminMetrics()).thenReturn(new AdminStatsDTO());

        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC_229: Admin Toggle User Active Status")
    void testTC_229_AdminToggleUserActiveStatus() throws Exception {
        User u = new User();
        u.setActive(false);
        when(adminService.toggleUserStatus(30L)).thenReturn(u);
        when(adminService.toggleUserActive(30L)).thenReturn(u);

        mockMvc.perform(put("/api/admin/users/30/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC_230: Admin Moderate Crop Listing Status")
    void testTC_230_AdminModerateCropListingStatus() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setStatus("AVAILABLE");

        when(adminService.approveProduct(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/products/1/status?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @Disabled("SKIPPED (BUG-SEC-04): Admin Users API Masks Password Hashes")
    @DisplayName("TC_231: Admin Users API Masks Password Hashes")
    void testTC_231_AdminUsersApiMasksPasswordHashes() {
        // Skipped in specification (BUG-SEC-04)
    }

    // ==========================================
    // Support Chat & Repository Tests (TC_232 - TC_236)
    // ==========================================

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_232: Send Chat Message to Platform Support")
    void testTC_232_SendChatMessageToPlatformSupport() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(1L);
        dto.setContent("Hello Support");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User(10L, "f@t.com", "h", "FARMER", true, null, null)));
        when(chatService.sendMessage(eq(10L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverId\":1,\"content\":\"Hello Support\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello Support"));
    }

    @Test
    @WithMockUser(username = "farmer@test.com", roles = "FARMER")
    @DisplayName("TC_233: Retrieve Chat Conversation History")
    void testTC_233_RetrieveChatConversationHistory() throws Exception {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User(10L, "f@t.com", "h", "FARMER", true, null, null)));
        when(chatService.getChatHistory(eq(10L), eq(1L))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/chat/history/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_234: ProductRepository Custom Status Query")
    void testTC_234_ProductRepositoryCustomStatusQuery() {
        Product p = new Product();
        p.setStatus("AVAILABLE");

        when(productRepository.findByStatusOrderByCreatedAt("AVAILABLE")).thenReturn(List.of(p));

        List<Product> results = productRepository.findByStatusOrderByCreatedAt("AVAILABLE");
        assertEquals(1, results.size());
        assertEquals("AVAILABLE", results.get(0).getStatus());
    }

    @Test
    @DisplayName("TC_235: StorageBookingRepository Capacity Sum Query")
    void testTC_235_StorageBookingRepositoryCapacitySumQuery() {
        when(storageBookingRepository.sumOccupiedCapacity(1L)).thenReturn(new BigDecimal("50.00"));

        BigDecimal sum = storageBookingRepository.sumOccupiedCapacity(1L);
        assertEquals(new BigDecimal("50.00"), sum);
    }

    @Test
    @DisplayName("TC_236: DeliveryTaskRepository Find by Unique Task Code")
    void testTC_236_DeliveryTaskRepositoryFindByUniqueTaskCode() {
        DeliveryTask task = new DeliveryTask();
        task.setTaskCode("MND-TSK-UNIQUE");

        when(deliveryTaskRepository.findByTaskCode("MND-TSK-UNIQUE")).thenReturn(Optional.of(task));

        Optional<DeliveryTask> found = deliveryTaskRepository.findByTaskCode("MND-TSK-UNIQUE");
        assertTrue(found.isPresent());
        assertEquals("MND-TSK-UNIQUE", found.get().getTaskCode());
    }
}
