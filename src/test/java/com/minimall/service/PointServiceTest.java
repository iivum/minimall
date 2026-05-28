package com.minimall.service;

import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.model.User;
import com.minimall.repository.PointAccountRepository;
import com.minimall.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("getAccount returns existing account")
    void getAccount_existingAccount_returnsAccount() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("100"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.getAccount("user-1");

        assertEquals("acc-1", result.getId());
        assertEquals(new BigDecimal("100"), result.getBalance());
    }

    @Test
    @DisplayName("getAccount creates account if not exists")
    void getAccount_notExists_createsAccount() {
        User user = new User();
        user.setId("user-1");

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> {
            PointAccount acc = i.getArgument(0);
            acc.setId("new-acc");
            return acc;
        });

        PointAccount result = pointService.getAccount("user-1");

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository).save(any(PointAccount.class));
    }

    @Test
    @DisplayName("getAccountByUserId throws when account not found")
    void getAccountByUserId_notFound_throws() {
        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pointService.getAccountByUserId("user-1"));
    }

    @Test
    @DisplayName("signIn adds sign in points")
    void signIn_addsPoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("50"));
        account.setTotalEarned(new BigDecimal("50"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.signIn("user-1");

        assertEquals(new BigDecimal("60"), result.getBalance());
        assertEquals(new BigDecimal("60"), result.getTotalEarned());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("earnShareReward adds share points")
    void earnShareReward_addsPoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("10"));
        account.setTotalEarned(new BigDecimal("10"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.earnShareReward("user-1", "share-123");

        assertEquals(new BigDecimal("15"), result.getBalance());
        assertEquals(new BigDecimal("15"), result.getTotalEarned());
    }

    @Test
    @DisplayName("earnOrderPoints calculates 1% of order amount")
    void earnOrderPoints_calculatesOnePercent() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.earnOrderPoints("user-1", "ORD-001", new BigDecimal("500"));

        assertEquals(new BigDecimal("5"), result.getBalance());
        assertEquals(new BigDecimal("5"), result.getTotalEarned());
    }

    @Test
    @DisplayName("earnOrderPoints does not add points for zero amount")
    void earnOrderPoints_zeroAmount_noPoints() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("10"));
        account.setTotalEarned(new BigDecimal("10"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.earnOrderPoints("user-1", "ORD-001", BigDecimal.ZERO);

        assertEquals(new BigDecimal("10"), result.getBalance());
        verify(transactionRepository, never()).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("deduct throws when insufficient balance")
    void deduct_insufficientBalance_throws() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("5"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            pointService.deduct("user-1", 100, "order-1", "Test deduction"));
    }

    @Test
    @DisplayName("deduct subtracts points successfully")
    void deduct_success() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setBalance(new BigDecimal("100"));
        account.setTotalSpent(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.deduct("user-1", 30, "order-1", "Redeem for coupon");

        assertEquals(new BigDecimal("70"), result.getBalance());
        assertEquals(new BigDecimal("30"), result.getTotalSpent());
    }

    @Test
    @DisplayName("getHistory returns transaction history")
    void getHistory_returnsHistory() {
        PointAccount account = new PointAccount();
        account.setId("acc-1");

        PointTransaction tx = new PointTransaction();
        tx.setId("tx-1");
        tx.setPoints(10);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc("acc-1")).thenReturn(List.of(tx));

        List<PointTransaction> result = pointService.getHistory("user-1");

        assertEquals(1, result.size());
        assertEquals("tx-1", result.get(0).getId());
    }

    @Test
    @DisplayName("createAccount saves new account")
    void createAccount_savesNewAccount() {
        User user = new User();
        user.setId("user-1");

        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> {
            PointAccount acc = i.getArgument(0);
            acc.setId("new-acc");
            return acc;
        });

        PointAccount result = pointService.createAccount("user-1");

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }
}