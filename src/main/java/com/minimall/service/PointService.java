package com.minimall.service;

import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.model.User;
import com.minimall.repository.PointAccountRepository;
import com.minimall.repository.PointTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PointService {
    private final PointAccountRepository accountRepository;
    private final PointTransactionRepository transactionRepository;
    private final UserService userService;

    public static final int SIGN_IN_POINTS = 10;
    public static final int SHARE_POINTS = 5;
    public static final BigDecimal ORDER_POINTS_RATE = new BigDecimal("0.01");

    public PointService(PointAccountRepository accountRepository,
                       PointTransactionRepository transactionRepository,
                       UserService userService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public PointAccount getAccount(String userId) {
        return accountRepository.findByUserId(userId)
            .orElseGet(() -> createAccount(userId));
    }

    public PointAccount getAccountByUserId(String userId) {
        return accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Point account not found for user: " + userId));
    }

    @Transactional
    public PointAccount createAccount(String userId) {
        User user = userService.findById(userId);
        PointAccount account = new PointAccount();
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);
        account.setTotalSpent(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public List<PointTransaction> getHistory(String userId) {
        PointAccount account = getAccountByUserId(userId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
    }

    @Transactional
    public PointAccount signIn(String userId) {
        PointAccount account = getAccount(userId);
        int points = SIGN_IN_POINTS;

        PointTransaction tx = new PointTransaction();
        tx.setAccount(account);
        tx.setAmount(BigDecimal.ZERO);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(points);
        tx.setDescription("每日签到奖励");
        transactionRepository.save(tx);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(points)));
        account.setTotalEarned(account.getTotalEarned().add(BigDecimal.valueOf(points)));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount earnShareReward(String userId, String shareId) {
        PointAccount account = getAccount(userId);
        int points = SHARE_POINTS;

        PointTransaction tx = new PointTransaction();
        tx.setAccount(account);
        tx.setAmount(BigDecimal.ZERO);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(points);
        tx.setOrderNo(shareId);
        tx.setDescription("分享商品奖励");
        transactionRepository.save(tx);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(points)));
        account.setTotalEarned(account.getTotalEarned().add(BigDecimal.valueOf(points)));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount earnOrderPoints(String userId, String orderNo, BigDecimal orderAmount) {
        PointAccount account = getAccount(userId);
        int points = orderAmount.multiply(ORDER_POINTS_RATE)
            .setScale(0, RoundingMode.DOWN).intValue();

        if (points <= 0) {
            return account;
        }

        PointTransaction tx = new PointTransaction();
        tx.setAccount(account);
        tx.setAmount(orderAmount);
        tx.setType(PointTransaction.Type.EARN);
        tx.setPoints(points);
        tx.setOrderNo(orderNo);
        tx.setDescription("订单完成奖励 (订单金额 × 1%)");
        transactionRepository.save(tx);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(points)));
        account.setTotalEarned(account.getTotalEarned().add(BigDecimal.valueOf(points)));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount deduct(String userId, int points, String orderNo, String description) {
        PointAccount account = getAccountByUserId(userId);

        if (account.getBalance().compareTo(BigDecimal.valueOf(points)) < 0) {
            throw new RuntimeException("Insufficient points balance");
        }

        PointTransaction tx = new PointTransaction();
        tx.setAccount(account);
        tx.setAmount(BigDecimal.ZERO);
        tx.setType(PointTransaction.Type.SPEND);
        tx.setPoints(points);
        tx.setOrderNo(orderNo);
        tx.setDescription(description);
        transactionRepository.save(tx);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(points)));
        account.setTotalSpent(account.getTotalSpent().add(BigDecimal.valueOf(points)));
        return accountRepository.save(account);
    }
}