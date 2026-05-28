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

    @Mock
    private PointAccountRepository accountRepository;

    @Mock
    private PointTransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(accountRepository, transactionRepository, userService);
    }

    @Test
    void getAccount_returnsExistingAccount() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(100));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.getAccount("user-1");

        assertEquals("acc-1", result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getBalance());
    }

    @Test
    void getAccount_createsNewAccountWhenNotFound() {
        User user = new User();
        user.setId("user-1");

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> {
            PointAccount saved = i.getArgument(0);
            saved.setId("new-acc-1");
            return saved;
        });

        PointAccount result = pointService.getAccount("user-1");

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository).save(any(PointAccount.class));
    }

    @Test
    void getAccountByUserId_throwsWhenNotFound() {
        when(accountRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pointService.getAccountByUserId("unknown"));
    }

    @Test
    void signIn_addsPointsAndReturnsAccount() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.signIn("user-1");

        assertEquals(BigDecimal.valueOf(10), result.getBalance());
        assertEquals(BigDecimal.valueOf(10), result.getTotalEarned());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void earnShareReward_addsPoints() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.earnShareReward("user-1", "share-123");

        assertEquals(BigDecimal.valueOf(5), result.getBalance());
        assertEquals(BigDecimal.valueOf(5), result.getTotalEarned());
    }

    @Test
    void earnOrderPoints_calculatesPointsCorrectly() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.earnOrderPoints("user-1", "order-123", BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(5), result.getBalance());
        assertEquals(BigDecimal.valueOf(5), result.getTotalEarned());
    }

    @Test
    void earnOrderPoints_skipsWhenAmountTooSmall() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(10));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.earnOrderPoints("user-1", "order-123", BigDecimal.valueOf(1));

        assertEquals(BigDecimal.valueOf(10), result.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deduct_throwsWhenInsufficientBalance() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(5));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pointService.getAccountByUserId("user-1"));
    }

    @Test
    void deduct_deductsPointsSuccessfully() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(100));
        account.setTotalSpent(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.deduct("user-1", 30, "order-123", "Test deduction");

        assertEquals(BigDecimal.valueOf(70), result.getBalance());
        assertEquals(BigDecimal.valueOf(30), result.getTotalSpent());
    }

    @Test
    void getHistory_returnsTransactions() {
        User user = new User();
        user.setId("user-1");

        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);

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