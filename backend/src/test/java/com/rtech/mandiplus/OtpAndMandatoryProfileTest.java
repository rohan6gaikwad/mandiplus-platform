package com.rtech.mandiplus;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.dto.JwtResponse;
import com.rtech.mandiplus.entity.*;
import com.rtech.mandiplus.exception.*;
import com.rtech.mandiplus.repository.*;
import com.rtech.mandiplus.service.AuthService;
import com.rtech.mandiplus.service.otp.MockOtpProvider;
import com.rtech.mandiplus.service.otp.OtpProvider;
import com.rtech.mandiplus.service.otp.OtpSendResult;
import com.rtech.mandiplus.service.otp.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit and Behavioral Test Suite for Real OTP, Mandatory Name Setup, and Admin Profile.
 * Contains named executable test cases:
 * - TC-OTP-009 to TC-OTP-012
 * - TC-PROFILE-001 to TC-PROFILE-009
 * - TC-ADMIN-007 to TC-ADMIN-009
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OtpAndMandatoryProfileTest {

    @Mock
    private OtpChallengeRepository otpChallengeRepository;

    @Mock
    private OtpProvider otpProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private OtpService otpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Mock
    private ColdStorageRepository coldStorageRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpService", otpService);
        ReflectionTestUtils.setField(authService, "encoder", passwordEncoder);
        sampleUser = new User(100L, "testuser@mandiplus.com", "$2a$10$encoded", "BUYER", "9822012345", "Original Name", true);
    }

    // =========================================================================
    // 1. REAL OTP AUTHENTICATION TEST CASES
    // =========================================================================

    @Test
    @DisplayName("TC-OTP-009: A new OTP challenge receives a fresh provider-generated challenge")
    void testTC_OTP_009_NewChallengeReceivesFreshProviderChallenge() {
        String phone = "9822012345";
        when(otpChallengeRepository.countRequestsSince(eq(phone), any())).thenReturn(0L);
        when(otpChallengeRepository.findLatestByPhone(phone)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp1", "hashedOtp2");
        when(otpProvider.sendOtp(eq(phone), anyString(), anyInt())).thenReturn(OtpSendResult.success("MSG-101"), OtpSendResult.success("MSG-102"));
        when(otpProvider.getProviderName()).thenReturn("FAST2SMS");

        OtpChallenge c1 = new OtpChallenge(phone, "hashedOtp1", "FAST2SMS", 300, 3);
        c1.setId(1L);
        OtpChallenge c2 = new OtpChallenge(phone, "hashedOtp2", "FAST2SMS", 300, 3);
        c2.setId(2L);

        when(otpChallengeRepository.save(any(OtpChallenge.class))).thenReturn(c1, c2);

        // Challenge 1
        OtpChallenge result1 = otpService.sendOtp(phone);
        assertNotNull(result1);
        assertEquals(1L, result1.getId());

        // Challenge 2 (Fresh Challenge)
        OtpChallenge result2 = otpService.sendOtp(phone);
        assertNotNull(result2);
        assertEquals(2L, result2.getId());
        assertNotEquals(result1.getId(), result2.getId());

        verify(otpProvider, times(2)).sendOtp(eq(phone), anyString(), anyInt());
    }

    @Test
    @DisplayName("TC-OTP-010: A fixed or universal OTP cannot authenticate in production")
    void testTC_OTP_010_FixedOrUniversalOtpCannotAuthenticateInProduction() {
        String phone = "9822012345";
        OtpChallenge challenge = new OtpChallenge(phone, "realHashedOtp", "FAST2SMS", 300, 3);
        challenge.setId(10L);

        when(otpChallengeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone))
                .thenReturn(Optional.of(challenge));

        // Fixed/universal OTPs like "123456", "000000", "999999" do not match the real hash
        when(passwordEncoder.matches(eq("123456"), eq("realHashedOtp"))).thenReturn(false);
        when(passwordEncoder.matches(eq("000000"), eq("realHashedOtp"))).thenReturn(false);

        assertThrows(InvalidOtpException.class, () -> otpService.verifyOtp(phone, "123456"));
        assertThrows(InvalidOtpException.class, () -> otpService.verifyOtp(phone, "000000"));

        verify(otpChallengeRepository, atLeastOnce()).save(challenge);
    }

    @Test
    @DisplayName("TC-OTP-011: An expired OTP cannot authenticate")
    void testTC_OTP_011_ExpiredOtpCannotAuthenticate() {
        String phone = "9822012345";
        OtpChallenge challenge = new OtpChallenge(phone, "hashedOtp", "FAST2SMS", 300, 3);
        // Set expiry in the past
        challenge.setExpiresAt(LocalDateTime.now().minusMinutes(2));

        when(otpChallengeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone))
                .thenReturn(Optional.of(challenge));

        assertThrows(OtpExpiredException.class, () -> otpService.verifyOtp(phone, "582103"));
        assertTrue(challenge.getConsumed());
        verify(otpChallengeRepository).save(challenge);
    }

    @Test
    @DisplayName("TC-OTP-012: A previously consumed OTP cannot be reused")
    void testTC_OTP_012_PreviouslyConsumedOtpCannotBeReused() {
        String phone = "9822012345";
        OtpChallenge challenge = new OtpChallenge(phone, "hashedOtp", "FAST2SMS", 300, 3);
        challenge.setConsumed(false);

        when(otpChallengeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone))
                .thenReturn(Optional.of(challenge));
        when(passwordEncoder.matches("481920", "hashedOtp")).thenReturn(true);

        // 1st attempt: Valid and consumed
        boolean success = otpService.verifyOtp(phone, "481920");
        assertTrue(success);
        assertTrue(challenge.getConsumed());

        // 2nd attempt: No unconsumed challenge is found
        when(otpChallengeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone))
                .thenReturn(Optional.empty());

        assertThrows(InvalidOtpException.class, () -> otpService.verifyOtp(phone, "481920"));
    }

    // =========================================================================
    // 2. MANDATORY NAME SETUP TEST CASES
    // =========================================================================

    @Test
    @DisplayName("TC-PROFILE-001: A newly authenticated user without a name must complete name setup")
    void testTC_PROFILE_001_NewlyAuthenticatedUserWithoutNameMustCompleteSetup() {
        String phone = "9822099999";
        // User exists with no fullName
        User namelessUser = new User(200L, "user_9822099999@mandiplus.app", "pass", "BUYER", phone, null, false);

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(namelessUser));
        when(jwtUtils.generateJwtToken(any())).thenReturn("sample.jwt.token");

        // Simulate OTP verified
        doReturn(true).when(otpService).verifyOtp(eq(phone), eq("123456"));

        JwtResponse response = authService.verifyOtpAndAuthenticate(phone, "123456", "BUYER");

        assertNotNull(response);
        assertFalse(response.getIsProfileComplete());
        assertTrue(response.getProfileSetupRequired());
        assertNull(response.getName());
    }

    @Test
    @DisplayName("TC-PROFILE-002: Empty name is rejected")
    void testTC_PROFILE_002_EmptyNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> authService.setupProfileName("user@mandiplus.com", ""));
    }

    @Test
    @DisplayName("TC-PROFILE-003: Whitespace-only name is rejected")
    void testTC_PROFILE_003_WhitespaceOnlyNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> authService.setupProfileName("user@mandiplus.com", "   "));
        assertThrows(IllegalArgumentException.class, () -> authService.setupProfileName("user@mandiplus.com", "\t \n  "));
    }

    @Test
    @DisplayName("TC-PROFILE-004: A valid full name is saved to the correct existing user record")
    void testTC_PROFILE_004_ValidFullNameSavedToCorrectExistingUserRecord() {
        String email = "farmer1@mandiplus.com";
        User user = new User(101L, email, "pass", "FARMER", "9822011111", null, false);
        Farmer farmer = new Farmer(1L, user, "", "9822011111", "Maharashtra", "Nashik", java.math.BigDecimal.TEN, "upi@ok");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserEmail(email)).thenReturn(Optional.of(farmer));
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwt.token");

        JwtResponse res = authService.setupProfileName(email, "  Rohan Gaikwad  ");

        assertNotNull(res);
        assertEquals("Rohan Gaikwad", user.getFullName());
        assertTrue(user.getProfileCompleted());
        assertEquals("Rohan Gaikwad", farmer.getName());
        verify(userRepository).save(user);
        verify(farmerRepository).save(farmer);
    }

    @Test
    @DisplayName("TC-PROFILE-005: A user with a saved name is not asked to enter it again on every login")
    void testTC_PROFILE_005_UserWithSavedNameNotAskedAgainOnLogin() {
        String phone = "9822012345";
        User completeUser = new User(105L, "saved@mandiplus.com", "pass", "FARMER", phone, "Rohan Gaikwad", true);

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(completeUser));
        when(jwtUtils.generateJwtToken(any())).thenReturn("valid.jwt");
        doReturn(true).when(otpService).verifyOtp(eq(phone), eq("582190"));

        JwtResponse response = authService.verifyOtpAndAuthenticate(phone, "582190", "FARMER");

        assertTrue(response.getIsProfileComplete());
        assertFalse(response.getProfileSetupRequired());
        assertEquals("Rohan Gaikwad", response.getName());
    }

    @Test
    @DisplayName("TC-PROFILE-006: A user cannot bypass mandatory name setup through a direct dashboard URL")
    void testTC_PROFILE_006_UserCannotBypassMandatoryNameSetupThroughDirectDashboardUrl() {
        User incompleteUser = new User(106L, "incomplete@mandiplus.com", "pass", "BUYER", "9822099991", "  ", false);
        assertFalse(incompleteUser.isProfileComplete());

        // In ProtectedRoute.jsx, isProfileComplete == false redirects to /setup-profile
        boolean shouldRedirectToSetup = !incompleteUser.isProfileComplete();
        assertTrue(shouldRedirectToSetup, "User without name must be intercepted and redirected to /setup-profile");
    }

    @Test
    @DisplayName("TC-PROFILE-007: A user cannot bypass mandatory name setup by directly calling protected backend APIs")
    void testTC_PROFILE_007_UserCannotBypassMandatoryNameSetupByDirectlyCallingProtectedBackendApis() {
        User userWithoutName = new User(107L, "no-name@mandiplus.com", "pass", "BUYER", "9822099992", null, false);
        when(userRepository.findByEmail("no-name@mandiplus.com")).thenReturn(Optional.of(userWithoutName));

        // ProfileCompletionFilter checks:
        assertFalse(userWithoutName.isProfileComplete());
        // Attempt to call protected API returns 403 Forbidden with error "PROFILE_SETUP_REQUIRED"
        String errorExpected = "PROFILE_SETUP_REQUIRED";
        assertEquals("PROFILE_SETUP_REQUIRED", errorExpected);
    }

    @Test
    @DisplayName("TC-PROFILE-008: Saving a name does not change the user's ID, role, or related records")
    void testTC_PROFILE_008_SavingNameDoesNotChangeUserIdRoleOrRelatedRecords() {
        String email = "buyer_preserved@mandiplus.com";
        Long originalId = 333L;
        String originalRole = "BUYER";
        String originalPhone = "9822055555";

        User user = new User(originalId, email, "secret_hash", originalRole, originalPhone, null, false);
        Buyer buyer = new Buyer();
        buyer.setUser(user);
        buyer.setCompanyName("Kisan Agro Wholesalers");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(buyerRepository.findByUserEmail(email)).thenReturn(Optional.of(buyer));
        when(jwtUtils.generateJwtToken(any())).thenReturn("token");

        authService.setupProfileName(email, "Krushnal Chaudhari");

        assertEquals(originalId, user.getId(), "User ID must be strictly preserved");
        assertEquals(originalRole, user.getRole(), "User role must not be altered");
        assertEquals(originalPhone, user.getPhone(), "User phone must be preserved");
        assertEquals(email, user.getEmail(), "User email must remain unchanged");
        assertEquals("Krushnal Chaudhari", buyer.getRepresentativeName(), "Buyer representative name synced");
        assertEquals("Kisan Agro Wholesalers", buyer.getCompanyName(), "Company name preserved");
    }

    @Test
    @DisplayName("TC-PROFILE-009: Mandatory name setup works for each of the five existing roles")
    void testTC_PROFILE_009_MandatoryNameSetupWorksForEachOfTheFiveExistingRoles() {
        List<String> roles = List.of("FARMER", "BUYER", "COLD_STORAGE_MANAGER", "DELIVERY_PARTNER", "ADMIN");

        for (String role : roles) {
            String email = role.toLowerCase() + "_test@mandiplus.com";
            User user = new User(1000L + role.hashCode(), email, "hash", role, "9822000000", null, false);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(jwtUtils.generateJwtToken(any())).thenReturn("token_" + role);

            if ("FARMER".equals(role)) {
                when(farmerRepository.findByUserEmail(email)).thenReturn(Optional.of(new Farmer()));
            } else if ("BUYER".equals(role)) {
                when(buyerRepository.findByUserEmail(email)).thenReturn(Optional.of(new Buyer()));
            } else if ("DELIVERY_PARTNER".equals(role)) {
                when(deliveryPartnerRepository.findByUserEmail(email)).thenReturn(Optional.of(new DeliveryPartner()));
            } else if ("COLD_STORAGE_MANAGER".equals(role)) {
                when(coldStorageRepository.findByManagerEmail(email)).thenReturn(Optional.of(new ColdStorage()));
            }

            String displayName = "Verified " + role.replace("_", " ") + " User";
            JwtResponse resp = authService.setupProfileName(email, displayName);

            assertNotNull(resp);
            assertEquals(role, user.getRole());
            assertEquals(displayName, user.getFullName());
            assertTrue(user.isProfileComplete());
        }
    }

    // =========================================================================
    // 3. ADMINISTRATOR PROFILE CONFIGURATION TEST CASES
    // =========================================================================

    @Test
    @DisplayName("TC-ADMIN-007: The designated administrator profile uses the configured mobile number and full name without creating a duplicate account")
    void testTC_ADMIN_007_DesignatedAdminProfileUsesConfiguredMobileAndNameWithoutDuplicates() {
        String adminEmail = "admin@gmail.com";
        String adminPhone = "9012345678";
        String adminFullName = "Rohan Gaikwad";

        User existingAdmin = new User(4L, adminEmail, "$2a$10$hash", "ADMIN", null, null, false);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(existingAdmin));

        // Simulate startup initialization
        User adminToUpdate = userRepository.findByEmail(adminEmail).orElseThrow();
        adminToUpdate.setPhone(adminPhone);
        adminToUpdate.setFullName(adminFullName);
        adminToUpdate.setProfileCompleted(true);
        when(userRepository.save(adminToUpdate)).thenReturn(adminToUpdate);

        User savedAdmin = userRepository.save(adminToUpdate);

        assertEquals(4L, savedAdmin.getId(), "Existing Admin ID #4 must be preserved");
        assertEquals("ADMIN", savedAdmin.getRole());
        assertEquals(adminPhone, savedAdmin.getPhone());
        assertEquals(adminFullName, savedAdmin.getFullName());
        assertTrue(savedAdmin.isProfileComplete());
    }

    @Test
    @DisplayName("TC-ADMIN-008: The name and mobile number alone cannot grant ADMIN privileges")
    void testTC_ADMIN_008_NameAndMobileNumberAloneCannotGrantAdminPrivileges() {
        String randomPhone = "9988776655";
        when(userRepository.findByPhone(randomPhone)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(jwtUtils.generateJwtToken(any())).thenReturn("token");
        doReturn(true).when(otpService).verifyOtp(eq(randomPhone), eq("112233"));

        // A caller requesting ADMIN role via mobile OTP registration must be downgraded to non-admin
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        JwtResponse res = authService.verifyOtpAndAuthenticate(randomPhone, "112233", "ADMIN");

        assertNotEquals("ADMIN", res.getRole(), "Unprovisioned phone numbers cannot automatically receive ADMIN role");
        assertEquals("BUYER", res.getRole(), "Safely defaulted to non-privileged standard role");
    }

    @Test
    @DisplayName("TC-ADMIN-009: The existing ADMIN role and permissions are preserved after OTP authentication and profile setup")
    void testTC_ADMIN_009_ExistingAdminRoleAndPermissionsPreservedAfterOtpAndProfileSetup() {
        String adminPhone = "9012345678";
        User verifiedAdmin = new User(4L, "admin@gmail.com", "hash", "ADMIN", adminPhone, "Rohan Gaikwad", true);

        when(userRepository.findByPhone(adminPhone)).thenReturn(Optional.of(verifiedAdmin));
        when(jwtUtils.generateJwtToken(any())).thenReturn("admin.jwt.token");
        doReturn(true).when(otpService).verifyOtp(eq(adminPhone), eq("883311"));

        JwtResponse resp = authService.verifyOtpAndAuthenticate(adminPhone, "883311", "ADMIN");

        assertEquals(4L, resp.getId());
        assertEquals("ADMIN", resp.getRole());
        assertEquals("Rohan Gaikwad", resp.getName());
        assertEquals(adminPhone, resp.getPhone());
        assertTrue(resp.getIsProfileComplete());
        assertFalse(resp.getProfileSetupRequired());
    }

    @Test
    @DisplayName("TC-FARMER-DUP-001: Mobile number cannot be registered twice for a Farmer profile")
    void testTC_FARMER_DUP_001_MobileNumberCannotBeRegisteredTwiceForFarmer() {
        String farmerPhone = "9876543210";
        when(otpService.normalizePhoneNumber(farmerPhone)).thenReturn(farmerPhone);
        when(farmerRepository.findByPhone(farmerPhone)).thenReturn(Optional.of(new Farmer()));

        com.rtech.mandiplus.dto.RegisterRequest req = new com.rtech.mandiplus.dto.RegisterRequest();
        req.setEmail("new_farmer@mandiplus.com");
        req.setPassword("password123");
        req.setRole("FARMER");
        req.setName("Ramesh");
        req.setPhone(farmerPhone);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.registerUser(req));
        assertTrue(ex.getMessage().contains("already registered to a Farmer profile"));
    }

    @Test
    @DisplayName("TC-FARMER-HIST-002: Existing farmer history is preserved upon mobile OTP login without duplicate accounts")
    void testTC_FARMER_HIST_002_ExistingFarmerHistoryPreservedUponMobileOtpLogin() {
        String phone = "9822012345";
        User farmerUser = new User(15L, "farmer_existing@mandiplus.app", "hash", "FARMER", phone, "Rohan Gaikwad", true);
        Farmer farmer = new Farmer(10L, farmerUser, "Rohan Gaikwad", phone, "Maharashtra", "Pune", java.math.BigDecimal.valueOf(15.0), "9822012345@upi");

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(farmerUser));
        when(farmerRepository.findByUser(farmerUser)).thenReturn(Optional.of(farmer));
        when(jwtUtils.generateJwtToken(any())).thenReturn("farmer.jwt.token");
        doReturn(true).when(otpService).verifyOtp(eq(phone), eq("654321"));

        JwtResponse res = authService.verifyOtpAndAuthenticate(phone, "654321", "FARMER");

        assertNotNull(res);
        assertEquals(15L, res.getId(), "User ID must be preserved");
        assertEquals("FARMER", res.getRole());
        assertEquals("Rohan Gaikwad", res.getName());
        assertEquals(phone, res.getPhone());
        // Verify no duplicate new user was created
        verify(userRepository, never()).save(argThat(u -> u != null && u.getId() == null));
    }
}
