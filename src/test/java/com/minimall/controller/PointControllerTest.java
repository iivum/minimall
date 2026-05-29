package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DeductPointsRequest;
import com.minimall.dto.PointAccountResponse;
import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.model.User;
import com.minimall.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointControllerTest {

    @Mock
    private PointService pointService;

    @Mock
    private SecurityUtils securityUtils;

    private PointController pointController;

    @BeforeEach
    void setUp() {
        pointController = new PointController(pointService, securityUtils);
    }

    private PointAccount createAccountWithUser(String userId, BigDecimal balance) {
        PointAccount account = new PointAccount();
        User user = new User();
        user.setId(userId);
        account.setUser(user);
        account.setBalance(balance);
        account.setTotalEarned(balance);
        account.setTotalSpent(BigDecimal.ZERO);
        return account;
    }

    @Test
    @DisplayName("getCurrentAccount returns current user account")
    void getCurrentAccount_success() {
        PointAccount account = createAccountWithUser("user-1", new BigDecimal("100"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getAccount("user-1")).thenReturn(account);

        ResponseEntity<PointAccountResponse> response = pointController.getCurrentAccount();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("100"), response.getBody().balance());
    }

    @Test
    @DisplayName("getAccount returns account by userId")
    void getAccount_success() {
        PointAccount account = createAccountWithUser("user-2", new BigDecimal("200"));

        when(pointService.getAccountByUserId("user-2")).thenReturn(account);

        ResponseEntity<PointAccountResponse> response = pointController.getAccount("user-2");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("signIn returns updated account")
    void signIn_success() {
        PointAccount account = createAccountWithUser("user-1", new BigDecimal("110"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.signIn("user-1")).thenReturn(account);

        ResponseEntity<PointAccountResponse> response = pointController.signIn();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("earnShareReward returns updated account")
    void earnShareReward_success() {
        PointAccount account = createAccountWithUser("user-1", new BigDecimal("105"));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.earnShareReward("user-1", "share-123")).thenReturn(account);

        ResponseEntity<PointAccountResponse> response =
            pointController.earnShareReward("share-123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("deduct deducts points")
    void deduct_success() {
        PointAccount account = createAccountWithUser("user-1", new BigDecimal("90"));

        DeductPointsRequest request = new DeductPointsRequest(10, "order-1", "测试扣减");

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.deduct("user-1", 10, "order-1", "测试扣减")).thenReturn(account);

        ResponseEntity<PointAccountResponse> response = pointController.deduct(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("getHistory returns transaction list")
    void getHistory_success() {
        PointTransaction tx = new PointTransaction();
        tx.setPoints(10);
        tx.setType(PointTransaction.Type.EARN);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(pointService.getHistory("user-1")).thenReturn(List.of(tx));

        var response = pointController.getCurrentHistory();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}