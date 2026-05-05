package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.MemberBenefitsResponse;
import com.minimall.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MembershipController.class)
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private SecurityUtils securityUtils;

    @Test
    @WithMockUser
    void getBenefits_returnsMemberBenefits() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(memberService.getBenefits("user-123"))
            .thenReturn(new MemberBenefitsResponse(
                "L1", "Basic Member", 0, 1,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1000),
                "Silver Member"
            ));

        mockMvc.perform(get("/api/membership/benefits"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void redeem_returnsSuccessMessage() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-123");

        mockMvc.perform(post("/api/membership/redeem")
                .contentType("application/json")
                .content("{\"benefitType\":\"points\",\"amount\":\"50\"}"))
            .andExpect(status().isOk());
    }
}