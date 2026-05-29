package com.minimall.controller;

import com.minimall.service.MemberService;
import com.minimall.config.SecurityUtils;
import com.minimall.dto.MemberBenefitsResponse;
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

    @Test
    @WithMockUser
    void getBenefits_returnsCurrentUserBenefits() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        MemberBenefitsResponse benefits = new MemberBenefitsResponse(
            "GOLD", "Gold会员", 10, 2, BigDecimal.valueOf(1000),
            BigDecimal.valueOf(5000), "PLATINUM"
        );
        when(memberService.getBenefits("user-1")).thenReturn(benefits);

        mockMvc.perform(get("/api/membership/benefits"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void redeem_returnsSuccessMessage() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");

        String requestBody = """
            {"benefitType": "DISCOUNT", "amount": 100}
            """;

        mockMvc.perform(post("/api/membership/redeem")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Redemption successful"));
    }
}