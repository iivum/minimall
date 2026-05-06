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

    private User createTestUser(String id) {
        User user = new User();
        user.setId(id);
        user.setOpenid("test-openid");
        user.setNickname("Test User");
        user.setPhone("1234567890");
        return user;
    }

    private PointAccount createTestAccount(String id, String userId) {
        PointAccount account = new PointAccount();
        account.setId(id);
        account.setUser(createTestUser(userId));
        account.setBalance(BigDecimal.valueOf(100));
        account.setTotalEarned(BigDecimal.valueOf(150));
        account.setTotalSpent(BigDecimal.valueOf(50));
        return account;
    }

    private PointTransaction createTestTransaction(String id, int points) {
        PointTransaction tx = new PointTransaction();
        tx.setId(id);
        tx.setAmount(BigDecimal.ZERO);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(points);
        tx.setDescription("Test transaction");
        return tx;
    }

    @Test
    @WithMockUser
    void getCurrentAccount_returnsAccount_whenAuthenticated() throws Exception {
        String userId = "user-123";
        PointAccount account = createTestAccount("account-1", userId);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.getAccount(userId)).thenReturn(account);

        mockMvc.perform(get("/api/points/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("account-1"))
            .andExpect(jsonPath("$.userId").value(userId))
            .andExpect(jsonPath("$.balance").value(100))
            .andExpect(jsonPath("$.totalEarned").value(150))
            .andExpect(jsonPath("$.totalSpent").value(50));
    }

    @Test
    @WithMockUser
    void getAccountByUserId_returnsAccount() throws Exception {
        String userId = "user-123";
        PointAccount account = createTestAccount("account-1", userId);
        when(pointService.getAccountByUserId(userId)).thenReturn(account);

        mockMvc.perform(get("/api/points/account/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("account-1"))
            .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @WithMockUser
    void getCurrentHistory_returnsTransactionList_whenAuthenticated() throws Exception {
        String userId = "user-123";
        PointTransaction tx1 = createTestTransaction("tx-1", 10);
        PointTransaction tx2 = createTestTransaction("tx-2", 5);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.getHistory(userId)).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/api/points/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("tx-1"))
            .andExpect(jsonPath("$[0].points").value(10))
            .andExpect(jsonPath("$[1].id").value("tx-2"))
            .andExpect(jsonPath("$[1].points").value(5));
    }

    @Test
    @WithMockUser
    void getHistoryByUserId_returnsTransactionList() throws Exception {
        String userId = "user-123";
        PointTransaction tx1 = createTestTransaction("tx-1", 10);
        when(pointService.getHistory(userId)).thenReturn(List.of(tx1));

        mockMvc.perform(get("/api/points/history/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("tx-1"));
    }

    @Test
    @WithMockUser
    void signIn_returnsUpdatedAccount_whenAuthenticated() throws Exception {
        String userId = "user-123";
        PointAccount account = createTestAccount("account-1", userId);
        account.setBalance(BigDecimal.valueOf(110));
        account.setTotalEarned(BigDecimal.valueOf(160));
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.signIn(userId)).thenReturn(account);

        mockMvc.perform(post("/api/points/sign-in")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(110))
            .andExpect(jsonPath("$.totalEarned").value(160));
    }

    @Test
    @WithMockUser
    void earnShareReward_returnsUpdatedAccount_whenAuthenticated() throws Exception {
        String userId = "user-123";
        String shareId = "share-456";
        PointAccount account = createTestAccount("account-1", userId);
        account.setBalance(BigDecimal.valueOf(105));
        account.setTotalEarned(BigDecimal.valueOf(155));
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.earnShareReward(userId, shareId)).thenReturn(account);

        mockMvc.perform(post("/api/points/earn/share/{shareId}", shareId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(105));
    }

    @Test
    @WithMockUser
    void deduct_returnsUpdatedAccount_whenAuthenticated() throws Exception {
        String userId = "user-123";
        PointAccount account = createTestAccount("account-1", userId);
        account.setBalance(BigDecimal.valueOf(90));
        account.setTotalSpent(BigDecimal.valueOf(60));
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.deduct(eq(userId), eq(10), any(), any())).thenReturn(account);

        String requestBody = """
            {
                "points": 10,
                "orderNo": "ORDER-123",
                "description": "Test deduction"
            }
            """;

        mockMvc.perform(post("/api/points/deduct")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(90))
            .andExpect(jsonPath("$.totalSpent").value(60));
    }

    @Test
    @WithMockUser
    void redeemCoupon_returnsUpdatedAccount_whenAuthenticated() throws Exception {
        String userId = "user-123";
        PointAccount account = createTestAccount("account-1", userId);
        account.setBalance(BigDecimal.valueOf(85));
        account.setTotalSpent(BigDecimal.valueOf(65));
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(pointService.deduct(eq(userId), eq(15), eq("ORDER-123"), any())).thenReturn(account);

        String requestBody = """
            {
                "points": 15,
                "orderNo": "ORDER-123",
                "description": null
            }
            """;

        mockMvc.perform(post("/api/points/redeem/coupon")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(85));
    }

    @Test
    void getCurrentAccount_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/points/account"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void signIn_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/points/sign-in")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }
}
