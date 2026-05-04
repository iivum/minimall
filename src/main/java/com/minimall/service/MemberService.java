package com.minimall.service;

import com.minimall.model.MemberGrade;
import com.minimall.model.User;
import com.minimall.repository.MemberGradeRepository;
import com.minimall.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {
    private final MemberGradeRepository memberGradeRepository;
    private final UserRepository userRepository;

    public MemberService(MemberGradeRepository memberGradeRepository, UserRepository userRepository) {
        this.memberGradeRepository = memberGradeRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getBenefits(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MemberGrade grade = memberGradeRepository.findByCode(user.getMemberGrade())
                .orElseThrow(() -> new RuntimeException("Member grade not found"));

        Map<String, Object> benefits = new HashMap<>();
        benefits.put("currentGrade", grade.getCode());
        benefits.put("gradeName", grade.getName());
        benefits.put("discountPercent", grade.getDiscountPercent());
        benefits.put("pointMultiplier", grade.getPointMultiplier());
        benefits.put("totalSpent", user.getTotalSpent());

        return benefits;
    }

    public Map<String, Object> redeem(String userId, String rewardType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rewardType", rewardType);
        result.put("message", "Reward redeemed successfully");

        return result;
    }

    @Transactional
    public void updateTotalSpent(String userId, BigDecimal orderAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal newTotalSpent = user.getTotalSpent().add(orderAmount);
        user.setTotalSpent(newTotalSpent);

        memberGradeRepository.findGradeForAmount(newTotalSpent)
                .ifPresent(newGrade -> {
                    if (!newGrade.getCode().equals(user.getMemberGrade())) {
                        user.setMemberGrade(newGrade.getCode());
                    }
                });

        userRepository.save(user);
    }
}