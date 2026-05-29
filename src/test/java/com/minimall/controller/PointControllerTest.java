package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.PointAccountResponse;
import com.minimall.dto.PointTransactionResponse;
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

    private com.minimall.model.PointAccount createPointAccount(String id, String userId, BigDecimal balance) {
        com.minimall.model.User user = new com.minimall.model.User();
        user.setId(userId);
        com.minimall.model.PointAccount account = new com.minimall.model.PointAccount();
        account.setId(id);
        account.setUser(user);
        account.setBalance(balance);
        account.setTotalEarned(balance);
        account.setTotalSpent(BigDecimal.ZERO);
        return account;
    }

    private com.minimall.model.PointTransaction createPointTransaction(String id, BigDecimal amount, int points, String orderNo, String description) {
        com.minimall.model.PointTransaction txn = new com.minimall.model.PointTransaction();
        txn.setId(id);
        txn.setAmount(amount);
        txn.setType(com.minimall.model.PointTransaction.Type.EARN);
        txn.setPoints(points);
        txn.setOrderNo(orderNo);
        txn.setDescription(description);
        return txn;
    }

    @Test
    @WithMockUser
    void getCurrentAccount_returnsCurrentUserAccount() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getAccount("user-1")).thenReturn(createPointAccount("acc-1", "user-1", BigDecimal.valueOf(500)));

        mockMvc.perform(get("/api/points/account"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAccountByUserId_returnsAccountForSpecificUser() throws Exception {
        when(pointService.getAccountByUserId("user-2")).thenReturn(createPointAccount("acc-2", "user-2", BigDecimal.valueOf(300)));

        mockMvc.perform(get("/api/points/account/user-2"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getCurrentHistory_returnsCurrentUserTransactions() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getHistory("user-1")).thenReturn(List.of(
            createPointTransaction("txn-1", BigDecimal.valueOf(10), 100, "ORD-001", "签到奖励")
        ));

        mockMvc.perform(get("/api/points/history"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getHistoryByUserId_returnsTransactionsForSpecificUser() throws Exception {
        when(pointService.getHistory("user-2")).thenReturn(List.of(
            createPointTransaction("txn-2", BigDecimal.valueOf(5), 50, "ORD-002", "分享奖励")
        ));

        mockMvc.perform(get("/api/points/history/user-2"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void signIn_returnsUpdatedAccount() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.signIn("user-1")).thenReturn(createPointAccount("acc-1", "user-1", BigDecimal.valueOf(510)));

        mockMvc.perform(post("/api/points/sign-in")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void earnShareReward_returnsUpdatedAccount() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.earnShareReward("user-1", "share-123")).thenReturn(createPointAccount("acc-1", "user-1", BigDecimal.valueOf(520)));

        mockMvc.perform(post("/api/points/earn/share/share-123")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deduct_returnsDeductedAccount() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct(eq("user-1"), eq(100), eq("ORD-001"), any())).thenReturn(createPointAccount("acc-1", "user-1", BigDecimal.valueOf(400)));

        String requestBody = """
            {"points": 100, "orderNo": "ORD-001", "description": "兑换优惠券"}
            """;

        mockMvc.perform(post("/api/points/deduct")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void redeemCoupon_returnsRedeemedAccount() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct(eq("user-1"), eq(200), eq("ORD-002"), eq("积分兑换优惠券"))).thenReturn(createPointAccount("acc-1", "user-1", BigDecimal.valueOf(200)));

        String requestBody = """
            {"points": 200, "orderNo": "ORD-002"}
            """;

        mockMvc.perform(post("/api/points/redeem/coupon")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }
}