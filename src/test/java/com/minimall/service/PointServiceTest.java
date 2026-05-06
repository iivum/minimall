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
        User user = new User();
        user.setId("user-1");
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("100"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.getAccount("user-1");

        assertNotNull(result);
        assertEquals("acc-1", result.getId());
        assertEquals(new BigDecimal("100"), result.getBalance());
    }

    @Test
    @DisplayName("getAccount creates new account if not exists")
    void getAccount_notExists_createsNew() {
        User user = new User();
        user.setId("user-1");

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userService.findById("user-1")).thenReturn(user);
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(invocation -> {
            PointAccount saved = invocation.getArgument(0);
            saved.setId("new-acc-1");
            return saved;
        });

        PointAccount result = pointService.getAccount("user-1");

        assertNotNull(result);
        verify(accountRepository).save(any(PointAccount.class));
    }

    @Test
    @DisplayName("signIn awards sign-in points")
    void signIn_awardsPoints() {
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

        assertEquals(new BigDecimal("10"), result.getBalance());
        assertEquals(new BigDecimal("10"), result.getTotalEarned());
        verify(transactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("earnOrderPoints calculates 1% of order amount")
    void earnOrderPoints_calculatesCorrectly() {
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

        PointAccount result = pointService.earnOrderPoints("user-1", "ORDER123", new BigDecimal("1000"));

        assertEquals(new BigDecimal("10"), result.getBalance());
        assertEquals(new BigDecimal("10"), result.getTotalEarned());
    }

    @Test
    @DisplayName("earnOrderPoints returns same account if points is zero")
    void earnOrderPoints_zeroAmount_returnsSame() {
        User user = new User();
        user.setId("user-1");
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        PointAccount result = pointService.earnOrderPoints("user-1", "ORDER123", new BigDecimal("10"));

        assertSame(account, result);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deduct throws exception if insufficient balance")
    void deduct_insufficientBalance_throwsException() {
        User user = new User();
        user.setId("user-1");
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("5"));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () ->
            pointService.deduct("user-1", 10, "ORDER123", "test"));
    }

    @Test
    @DisplayName("deduct reduces balance correctly")
    void deduct_sufficientBalance_reducesBalance() {
        User user = new User();
        user.setId("user-1");
        PointAccount account = new PointAccount();
        account.setId("acc-1");
        account.setUser(user);
        account.setBalance(new BigDecimal("100"));
        account.setTotalSpent(BigDecimal.ZERO);

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(PointTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(PointAccount.class))).thenAnswer(i -> i.getArgument(0));

        PointAccount result = pointService.deduct("user-1", 30, "ORDER123", "test");

        assertEquals(new BigDecimal("70"), result.getBalance());
        assertEquals(new BigDecimal("30"), result.getTotalSpent());
    }
}