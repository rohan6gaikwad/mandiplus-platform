package com.rtech.mandiplus.security;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.config.SecurityConfig;
import com.rtech.mandiplus.controller.GovMandiAdminController;
import com.rtech.mandiplus.controller.GovMandiRatesController;
import com.rtech.mandiplus.dto.GovMandiAdviceDTO;
import com.rtech.mandiplus.dto.GovMandiSyncStatusDTO;
import com.rtech.mandiplus.service.GovMandiAdminService;
import com.rtech.mandiplus.service.GovMandiAdvisorService;
import com.rtech.mandiplus.service.GovMandiSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GovMandiRatesController.class, GovMandiAdminController.class})
@Import(SecurityConfig.class)
public class GovMandiSecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovMandiAdvisorService advisorService;

    @MockBean
    private GovMandiAdminService adminService;

    @MockBean
    private GovMandiSyncService syncService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.rtech.mandiplus.repository.GovMandiMspRepository mspRepository;

    @MockBean
    private com.rtech.mandiplus.repository.GovMandiAdvisoryRepository advisoryRepository;

    // 1. Unauthenticated checks -> 401 Unauthorized
    @Test
    @DisplayName("Unauthenticated request to /api/mandi-rates/advice should return 401")
    void testUnauthenticatedAccessToMandiRates() throws Exception {
        mockMvc.perform(get("/api/mandi-rates/advice?commodity=Soyabean&district=Nashik&market=Lasalgaon"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /api/admin/mandi-management/sync-status should return 401")
    void testUnauthenticatedAccessToAdminMandiManagement() throws Exception {
        mockMvc.perform(get("/api/admin/mandi-management/sync-status"))
                .andExpect(status().isUnauthorized());
    }

    // 2. FARMER role checks
    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER role CAN access /api/mandi-rates/advice")
    void testFarmerCanAccessMandiRates() throws Exception {
        GovMandiAdviceDTO advice = new GovMandiAdviceDTO();
        advice.setCommodity("Soyabean");
        advice.setModalPrice(new BigDecimal("4950.00"));
        when(advisorService.getMandiAdvice(any(), any(), any(), any())).thenReturn(advice);

        mockMvc.perform(get("/api/mandi-rates/advice?commodity=Soyabean&district=Nashik&market=Lasalgaon"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "farmer1", roles = {"FARMER"})
    @DisplayName("FARMER role is FORBIDDEN (403) from accessing /api/admin/mandi-management")
    void testFarmerDeniedFromAdminMandiManagement() throws Exception {
        mockMvc.perform(get("/api/admin/mandi-management/sync-status"))
                .andExpect(status().isForbidden());
    }

    // 3. ADMIN role checks
    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("ADMIN role CAN access /api/mandi-rates/advice")
    void testAdminCanAccessMandiRates() throws Exception {
        GovMandiAdviceDTO advice = new GovMandiAdviceDTO();
        advice.setCommodity("Soyabean");
        when(advisorService.getMandiAdvice(any(), any(), any(), any())).thenReturn(advice);

        mockMvc.perform(get("/api/mandi-rates/advice?commodity=Soyabean&district=Nashik&market=Lasalgaon"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("ADMIN role CAN access /api/admin/mandi-management/sync-status")
    void testAdminCanAccessAdminMandiManagement() throws Exception {
        when(adminService.getSyncStatus()).thenReturn(new GovMandiSyncStatusDTO());

        mockMvc.perform(get("/api/admin/mandi-management/sync-status"))
                .andExpect(status().isOk());
    }

    // 4. BUYER role checks -> 403 Forbidden on both
    @Test
    @WithMockUser(username = "buyer1", roles = {"BUYER"})
    @DisplayName("BUYER role is FORBIDDEN (403) from accessing /api/mandi-rates/advice")
    void testBuyerDeniedFromMandiRates() throws Exception {
        mockMvc.perform(get("/api/mandi-rates/advice?commodity=Soyabean&district=Nashik&market=Lasalgaon"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "buyer1", roles = {"BUYER"})
    @DisplayName("BUYER role is FORBIDDEN (403) from accessing /api/admin/mandi-management")
    void testBuyerDeniedFromAdminMandiManagement() throws Exception {
        mockMvc.perform(get("/api/admin/mandi-management/sync-status"))
                .andExpect(status().isForbidden());
    }

    // 5. COLD_STORAGE_MANAGER role checks -> 403 Forbidden on both
    @Test
    @WithMockUser(username = "csm1", roles = {"COLD_STORAGE_MANAGER"})
    @DisplayName("COLD_STORAGE_MANAGER role is FORBIDDEN (403) from accessing /api/mandi-rates/advice")
    void testColdStorageManagerDeniedFromMandiRates() throws Exception {
        mockMvc.perform(get("/api/mandi-rates/advice?commodity=Soyabean&district=Nashik&market=Lasalgaon"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "csm1", roles = {"COLD_STORAGE_MANAGER"})
    @DisplayName("COLD_STORAGE_MANAGER role is FORBIDDEN (403) from accessing /api/admin/mandi-management")
    void testColdStorageManagerDeniedFromAdminMandiManagement() throws Exception {
        mockMvc.perform(get("/api/admin/mandi-management/sync-status"))
                .andExpect(status().isForbidden());
    }
}
