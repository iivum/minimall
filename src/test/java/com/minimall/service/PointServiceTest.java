package com.minimall.service;

import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.model.User;
import com.minimall.repository.PointAccountRepository;
import com.minimall.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock private PointAccountRepository accountRepository;
    @Mock private PointTransactionRepository transactionRepository;
    @Mock private UserService userService;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(accountRepository, transactionRepository, userService);
    }

    @Test
    void getAccount_returnsExistingAccount() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.valueOf(100));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.getAccount("user-1");

        assertEquals("acc-1", result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getBalance());
    }

    @Test
    void getAccount_createsNewAccountWhenNotExists() {
        User user = new User();
        user.setId("user-1");

        PointAccount newAccount = new PointAccount();
        newAccount.setId("new-acc");
        newAccount.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenReturn(newAccount);

        PointAccount result = pointService.getAccount("user-1");

        assertEquals("new-acc", result.getId());
        verify(accountRepository).save(any(PointAccount.class));
    }

    @Test
    void getAccountByUserId_throwsWhenNotFound() {
        when(accountRepository.findByUserId("invalid-user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pointService.getAccountByUserId("invalid-user"));
    }

    @Test
    void createAccount_savesNewAccount() {
        User user = new User();
        user.setId("user-1");

        PointAccount savedAccount = new PointAccount();
        savedAccount.setId("acc-1");
        savedAccount.setUser(user);
        savedAccount.setBalance(BigDecimal.ZERO);

        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenReturn(savedAccount);

        PointAccount result = pointService.createAccount("user-1");

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository).save(any(PointAccount.class));
    }

    @Test
    void signIn_awardsSignInPoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenReturn(new PointTransaction());
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        PointAccount result = pointService.signIn("user-1");

        assertEquals(BigDecimal.valueOf(PointService.SIGN_IN_POINTS), result.getBalance());
        assertEquals(BigDecimal.valueOf(PointService.SIGN_IN_POINTS), result.getTotalEarned());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void earnShareReward_awardsSharePoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenReturn(new PointTransaction());
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        PointAccount result = pointService.earnShareReward("user-1", "share-123");

        assertEquals(BigDecimal.valueOf(PointService.SHARE_POINTS), result.getBalance());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void earnOrderPoints_calculatesBasedOnOrderAmount() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenReturn(new PointTransaction());
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal orderAmount = BigDecimal.valueOf(1000);
        PointAccount result = pointService.earnOrderPoints("user-1", "order-1", orderAmount);

        // 1000 * 0.01 = 10 points
        assertEquals(BigDecimal.valueOf(10), result.getBalance());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void earnOrderPoints_returnsEarlyWhenNoPoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        BigDecimal orderAmount = BigDecimal.valueOf(50); // 50 * 0.01 = 0.5 -> 0 points
        PointAccount result = pointService.earnOrderPoints("user-1", "order-1", orderAmount);

        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deduct_throwsWhenInsufficientBalance() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.valueOf(5));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () ->
            pointService.deduct("user-1", 10, "order-1", "Test deduction")
        );
    }

    @Test
    void deduct_success() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.valueOf(100));
        account.setTotalSpent(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenReturn(new PointTransaction());
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        PointAccount result = pointService.deduct("user-1", 30, "order-1", "Points redemption");

        assertEquals(BigDecimal.valueOf(70), result.getBalance());
        assertEquals(BigDecimal.valueOf(30), result.getTotalSpent());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void getHistory_returnsTransactionsForAccount() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");

        PointTransaction tx = new PointTransaction();
        tx.setId("tx-1");
        tx.setPoints(10);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc("acc-1")).thenReturn(List.of(tx));

        List<PointTransaction> result = pointService.getHistory("user-1");

        assertEquals(1, result.size());
        assertEquals("tx-1", result.get(0).getId());
    }
}