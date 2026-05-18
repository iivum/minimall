package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DeductPointsRequest;
import com.minimall.dto.PointAccountResponse;
import com.minimall.dto.PointTransactionResponse;
import com.minimall.model.PointAccount;
import com.minimall.model.User;
import com.minimall.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(TestSecurityConfig.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private SecurityUtils securityUtils;

    @Test
    @WithMockUser
    void getCurrentAccount_returnsCurrentUserAccount() throws Exception {
        User user = new User();
        user.setId("user-123");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("150"));
        account.setTotalEarned(new BigDecimal("200"));
        account.setTotalSpent(new BigDecimal("50"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(pointService.getAccount("user-123")).thenReturn(account);

        mockMvc.perform(get("/api/points/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("user-123"))
            .andExpect(jsonPath("$.balance").value(150));
    }

    @Test
    @WithMockUser
    void getAccount_byUserId_returnsAccount() throws Exception {
        User user = new User();
        user.setId("user-456");

        PointAccount account = new PointAccount();
        account.setId("acc-2");
        account.setUser(user);
        account.setBalance(new BigDecimal("300"));
        account.setTotalEarned(new BigDecimal("400"));
        account.setTotalSpent(new BigDecimal("100"));

        when(pointService.getAccountByUserId("user-456")).thenReturn(account);

        mockMvc.perform(get("/api/points/account/user-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("user-456"))
            .andExpect(jsonPath("$.balance").value(300));
    }

    @Test
    @WithMockUser
    void getCurrentHistory_returnsCurrentUserTransactions() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-123");

        com.minimall.model.PointTransaction tx1 = new com.minimall.model.PointTransaction();
        tx1.setId("tx-1");
        tx1.setAmount(BigDecimal.ZERO);
        tx1.setType(com.minimall.model.PointTransaction.Type.EARN);
        tx1.setPoints(10);
        tx1.setDescription("每日签到奖励");

        com.minimall.model.PointTransaction tx2 = new com.minimall.model.PointTransaction();
        tx2.setId("tx-2");
        tx2.setAmount(new BigDecimal("50.00"));
        tx2.setType(com.minimall.model.PointTransaction.Type.EARN);
        tx2.setPoints(50);
        tx2.setOrderNo("ORD-001");
        tx2.setDescription("订单完成奖励");

        when(pointService.getHistory("user-123")).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/api/points/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("tx-1"))
            .andExpect(jsonPath("$[0].type").value("EARN"));
    }

    @Test
    @WithMockUser
    void getHistory_byUserId_returnsUserTransactions() throws Exception {
        com.minimall.model.PointTransaction tx3 = new com.minimall.model.PointTransaction();
        tx3.setId("tx-3");
        tx3.setAmount(BigDecimal.ZERO);
        tx3.setType(com.minimall.model.PointTransaction.Type.SPEND);
        tx3.setPoints(20);
        tx3.setOrderNo("ORD-002");
        tx3.setDescription("积分兑换");

        when(pointService.getHistory("user-456")).thenReturn(List.of(tx3));

        mockMvc.perform(get("/api/points/history/user-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("tx-3"))
            .andExpect(jsonPath("$[0].type").value("SPEND"));
    }

    @Test
    @WithMockUser
    void signIn_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-123");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("160"));
        account.setTotalEarned(new BigDecimal("210"));
        account.setTotalSpent(new BigDecimal("50"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(pointService.signIn("user-123")).thenReturn(account);

        mockMvc.perform(post("/api/points/sign-in")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(160));
    }

    @Test
    @WithMockUser
    void earnShareReward_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-123");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("165"));
        account.setTotalEarned(new BigDecimal("215"));
        account.setTotalSpent(new BigDecimal("50"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(pointService.earnShareReward("user-123", "share-456")).thenReturn(account);

        mockMvc.perform(post("/api/points/earn/share/share-456")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(165));
    }

    @Test
    @WithMockUser
    void deduct_withValidRequest_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-123");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("140"));
        account.setTotalEarned(new BigDecimal("210"));
        account.setTotalSpent(new BigDecimal("70"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(pointService.deduct(eq("user-123"), eq(20), eq("ORD-003"), eq("Testing deduction"))).thenReturn(account);

        String requestBody = """
            {
                "points": 20,
                "orderNo": "ORD-003",
                "description": "Testing deduction"
            }
            """;

        mockMvc.perform(post("/api/points/deduct")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(140));
    }

    @Test
    @WithMockUser
    void redeemCoupon_withValidRequest_returnsUpdatedAccount() throws Exception {
        User user = new User();
        user.setId("user-123");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("130"));
        account.setTotalEarned(new BigDecimal("210"));
        account.setTotalSpent(new BigDecimal("80"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-123");
        when(pointService.deduct(eq("user-123"), eq(10), eq("COUPON-123"), eq("积分兑换优惠券"))).thenReturn(account);

        String requestBody = """
            {
                "points": 10,
                "orderNo": "COUPON-123"
            }
            """;

        mockMvc.perform(post("/api/points/redeem/coupon")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(130));
    }
}