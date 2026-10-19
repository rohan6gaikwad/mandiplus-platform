package com.rtech.mandiplus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtech.mandiplus.dto.FirebaseLoginRequest;
import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.service.firebase.FirebaseTokenHolder;
import com.rtech.mandiplus.service.firebase.FirebaseTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FirebaseAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @MockBean
    private FirebaseTokenVerifier firebaseTokenVerifier;

    private static final String ADMIN_PHONE = "+919012345678";
    private static final String NORMAL_PHONE = "+919876543210";
    private static final String NEW_PHONE = "+919123456789";

    @BeforeEach
    void setupMocks() {
        // Mock Admin Phone Token
        when(firebaseTokenVerifier.verifyIdToken(eq("valid-admin-token")))
                .thenReturn(new FirebaseTokenHolder("admin-uid-123", ADMIN_PHONE, "admin@gmail.com", "Rohan Gaikwad",
                        Map.of("phone_number", ADMIN_PHONE, "sub", "admin-uid-123")));

        // Mock Normal Existing Phone Token
        when(firebaseTokenVerifier.verifyIdToken(eq("valid-normal-token")))
                .thenReturn(new FirebaseTokenHolder("normal-uid-456", NORMAL_PHONE, "farmer@mandiplus.app", "Test Farmer",
                        Map.of("phone_number", NORMAL_PHONE, "sub", "normal-uid-456")));

        // Mock New User Phone Token
        when(firebaseTokenVerifier.verifyIdToken(eq("valid-new-token")))
                .thenReturn(new FirebaseTokenHolder("new-uid-789", NEW_PHONE, null, null,
                        Map.of("phone_number", NEW_PHONE, "sub", "new-uid-789")));

        // Mock Invalid Token
        when(firebaseTokenVerifier.verifyIdToken(eq("invalid-token")))
                .thenThrow(new SecurityException("Invalid Firebase ID token signature"));
    }

    @Test
    @DisplayName("TC-VERIFY-001 & TC-JWT-001: Valid Firebase token produces authenticated application JWT")
    void testValidFirebaseTokenAuthenticates() throws Exception {
        FirebaseLoginRequest request = new FirebaseLoginRequest("valid-admin-token", "FARMER");

        mockMvc.perform(post("/api/auth/firebase/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.phone").value("9012345678"));
    }

    @Test
    @DisplayName("TC-VERIFY-011: Invalid Firebase token is rejected by Spring Boot")
    void testInvalidFirebaseTokenRejected() throws Exception {
        FirebaseLoginRequest request = new FirebaseLoginRequest("invalid-token", "BUYER");

        mockMvc.perform(post("/api/auth/firebase/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("FIREBASE_TOKEN_INVALID"));
    }

    @Test
    @DisplayName("TC-ADMIN-006 & TC-ADMIN-008: Ordinary user cannot escalate role to ADMIN via frontend parameter")
    void testNonAdminCannotEscalateRole() throws Exception {
        // Attempt to request ADMIN role with a new phone number
        FirebaseLoginRequest request = new FirebaseLoginRequest("valid-new-token", "ADMIN");

        mockMvc.perform(post("/api/auth/firebase/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BUYER")) // Downgraded safely, never ADMIN
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC-USER-003 & TC-USER-004: Existing user ID and profile data remain intact after login")
    void testExistingUserHistoryPreserved() throws Exception {
        Optional<User> adminOpt = userRepository.findByPhone("9012345678");
        assertTrue(adminOpt.isPresent(), "Admin user with phone 9012345678 must exist in DB");
        Long originalId = adminOpt.get().getId();

        FirebaseLoginRequest request = new FirebaseLoginRequest("valid-admin-token", "FARMER");

        mockMvc.perform(post("/api/auth/firebase/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(originalId.intValue()));

        // Verify ID did not change in DB
        User userAfter = userRepository.findById(originalId).orElseThrow();
        assertEquals("9012345678", userAfter.getPhone());
        assertEquals("ADMIN", userAfter.getRole());
    }
}
