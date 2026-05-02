package com.minimall.service;

import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.repository.PointAccountRepository;
import com.minimall.repository.PointTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class PointService {
    private final PointAccountRepository accountRepository;
    private final PointTransactionRepository transactionRepository;

    private static final BigDecimal SIGN_IN_POINTS = BigDecimal.valueOf(10);
    private static final BigDecimal SHARE_POINTS = BigDecimal.valueOf(5);
    private static final BigDecimal ORDER_REWARD_RATE = BigDecimal.valueOf(0.01);

    public PointService(PointAccountRepository accountRepository,
                        PointTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public PointAccount getAccount(String userId) {
        return accountRepository.findByUserId(userId)
            .orElseGet(() -> createAccount(userId));
    }

    private PointAccount createAccount(String userId) {
        PointAccount account = new PointAccount();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);
        account.setTotalSpent(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount signIn(String userId) {
        PointAccount account = getAccount(userId);

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setType(PointTransaction.Type.SIGN_IN);
        transaction.setAmount(SIGN_IN_POINTS);
        transaction.setDescription("每日签到奖励");
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().add(SIGN_IN_POINTS));
        account.setTotalEarned(account.getTotalEarned().add(SIGN_IN_POINTS));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount earnFromOrder(String userId, BigDecimal orderAmount, String orderId) {
        BigDecimal points = orderAmount.multiply(ORDER_REWARD_RATE)
            .setScale(2, RoundingMode.DOWN);

        PointAccount account = getAccount(userId);

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setType(PointTransaction.Type.ORDER_REWARD);
        transaction.setAmount(points);
        transaction.setDescription("订单完成奖励");
        transaction.setOrderId(orderId);
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().add(points));
        account.setTotalEarned(account.getTotalEarned().add(points));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount earnFromShare(String userId, String shareId) {
        PointAccount account = getAccount(userId);

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setType(PointTransaction.Type.SHARE);
        transaction.setAmount(SHARE_POINTS);
        transaction.setDescription("分享奖励");
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().add(SHARE_POINTS));
        account.setTotalEarned(account.getTotalEarned().add(SHARE_POINTS));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount deduct(String userId, BigDecimal amount, String description) {
        PointAccount account = getAccount(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("积分不足");
        }

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setType(PointTransaction.Type.SPEND);
        transaction.setAmount(amount.negate());
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalSpent(account.getTotalSpent().add(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public PointAccount redeemCoupon(String userId, String couponId) {
        PointAccount account = getAccount(userId);
        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setType(PointTransaction.Type.SPEND);
        transaction.setDescription("积分兑换优惠券: " + couponId);
        transactionRepository.save(transaction);
        return account;
    }

    public List<PointTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}