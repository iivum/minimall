package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DeductPointsRequest;
import com.minimall.dto.PointAccountResponse;
import com.minimall.dto.PointTransactionResponse;
import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.model.User;
import com.minimall.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void getCurrentAccount_returnsCurrentUserAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(500));
        account.setTotalEarned(BigDecimal.valueOf(1000));
        account.setTotalSpent(BigDecimal.valueOf(500));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getAccount("user-1")).thenReturn(account);

        mockMvc.perform(get("/api/points/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    @WithMockUser
    void getAccount_returnsUserAccount() throws Exception {
        User user = new User();
        user.setId("user-2");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(300));
        account.setTotalEarned(BigDecimal.valueOf(800));
        account.setTotalSpent(BigDecimal.valueOf(500));

        when(pointService.getAccountByUserId("user-2")).thenReturn(account);

        mockMvc.perform(get("/api/points/account/user-2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(300));
    }

    @Test
    @WithMockUser
    void getCurrentHistory_returnsCurrentUserHistory() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);

        PointTransaction tx = new PointTransaction();
        tx.setId("tx-1");
        tx.setAccount(account);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(100);
        tx.setDescription("Sign in bonus");

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getHistory("user-1")).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/points/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].points").value(100));
    }

    @Test
    @WithMockUser
    void getHistory_returnsUserHistory() throws Exception {
        User user = new User();
        user.setId("user-2");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);

        PointTransaction tx = new PointTransaction();
        tx.setId("tx-1");
        tx.setAccount(account);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(50);
        tx.setDescription("Share reward");

        when(pointService.getHistory("user-2")).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/points/history/user-2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].points").value(50));
    }

    @Test
    @WithMockUser
    void signIn_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(510));
        account.setTotalEarned(BigDecimal.valueOf(510));
        account.setTotalSpent(BigDecimal.ZERO);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.signIn("user-1")).thenReturn(account);

        mockMvc.perform(post("/api/points/sign-in")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(510));
    }

    @Test
    @WithMockUser
    void earnShareReward_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(600));
        account.setTotalEarned(BigDecimal.valueOf(600));
        account.setTotalSpent(BigDecimal.ZERO);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.earnShareReward("user-1", "share-123")).thenReturn(account);

        mockMvc.perform(post("/api/points/earn/share/share-123")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(600));
    }

    @Test
    @WithMockUser
    void deduct_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(400));
        account.setTotalEarned(BigDecimal.valueOf(600));
        account.setTotalSpent(BigDecimal.valueOf(200));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct(eq("user-1"), eq(200), eq("order-123"), any()))
            .thenReturn(account);

        String requestBody = """
            {
                "points": 200,
                "orderNo": "order-123",
                "description": "Order payment"
            }
            """;

        mockMvc.perform(post("/api/points/deduct")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(400));
    }

    @Test
    @WithMockUser
    void redeemCoupon_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(350));
        account.setTotalEarned(BigDecimal.valueOf(600));
        account.setTotalSpent(BigDecimal.valueOf(250));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct(eq("user-1"), eq(250), eq("coupon-redeem"), eq("积分兑换优惠券")))
            .thenReturn(account);

        String requestBody = """
            {
                "points": 250,
                "orderNo": "coupon-redeem"
            }
            """;

        mockMvc.perform(post("/api/points/redeem/coupon")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(350));
    }

    @Test
    @WithMockUser
    void redeemCoupon_withDescription_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(300));
        account.setTotalEarned(BigDecimal.valueOf(600));
        account.setTotalSpent(BigDecimal.valueOf(300));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct(eq("user-1"), eq(300), eq("coupon-redeem"), eq("VIP discount")))
            .thenReturn(account);

        String requestBody = """
            {
                "points": 300,
                "orderNo": "coupon-redeem",
                "description": "VIP discount"
            }
            """;

        mockMvc.perform(post("/api/points/redeem/coupon")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(300));
    }
}