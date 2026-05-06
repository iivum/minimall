package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.MemberBenefitsResponse;
import com.minimall.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembershipController.class)
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private MemberBenefitsResponse createTestBenefits() {
        return new MemberBenefitsResponse(
            "L1",
            "Level 1",
            99,
            1,
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(1000),
            "L2"
        );
    }

    @Test
    @WithMockUser
    void getBenefits_returnsBenefits_whenAuthenticated() throws Exception {
        String userId = "user-123";
        MemberBenefitsResponse benefits = createTestBenefits();
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(memberService.getBenefits(userId)).thenReturn(benefits);

        mockMvc.perform(get("/api/membership/benefits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gradeCode").value("L1"))
            .andExpect(jsonPath("$.gradeName").value("Level 1"))
            .andExpect(jsonPath("$.discountPercent").value(99))
            .andExpect(jsonPath("$.pointMultiplier").value(1))
            .andExpect(jsonPath("$.totalSpent").value(500))
            .andExpect(jsonPath("$.nextGradeThreshold").value(1000))
            .andExpect(jsonPath("$.nextGradeName").value("L2"));
    }

    @Test
    @WithMockUser
    void redeem_returnsSuccess_whenAuthenticated() throws Exception {
        String userId = "user-123";
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        mockMvc.perform(post("/api/membership/redeem")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "benefitType": "DISCOUNT",
                        "amount": 100
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Redemption successful"));
    }

    @Test
    void getBenefits_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/membership/benefits"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void redeem_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/membership/redeem")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "benefitType": "DISCOUNT",
                        "amount": 100
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }
}
