package com.minimall.service;

import com.minimall.dto.MemberBenefitsResponse;
import com.minimall.model.MemberGrade;
import com.minimall.model.User;
import com.minimall.repository.MemberGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberGradeRepository memberGradeRepository;
    private final UserService userService;

    public MemberService(MemberGradeRepository memberGradeRepository, UserService userService) {
        this.memberGradeRepository = memberGradeRepository;
        this.userService = userService;
    }

    public MemberBenefitsResponse getBenefits(String userId) {
        User user = userService.findById(userId);
        MemberGrade currentGrade = memberGradeRepository.findByCode(user.getMemberGrade())
            .orElseThrow(() -> new RuntimeException("Grade not found: " + user.getMemberGrade()));

        Optional<MemberGrade> nextGrade = memberGradeRepository.findAll().stream()
            .filter(g -> g.getMinAmount().compareTo(currentGrade.getMinAmount()) > 0)
            .findFirst();

        return MemberBenefitsResponse.from(user, currentGrade, nextGrade.orElse(null));
    }

    @Transactional
    public void redeem(String userId, String benefitType, BigDecimal amount) {
        User user = userService.findById(userId);
        // Benefit redemption logic - implementation depends on specific requirements
        // This is a placeholder for actual redemption logic
    }

    @Transactional
    public void updateTotalSpent(String userId, BigDecimal orderAmount) {
        User user = userService.findById(userId);
        BigDecimal newTotal = user.getTotalSpent().add(orderAmount);
        user.setTotalSpent(newTotal);

        // Check if user qualifies for a higher grade
        Optional<MemberGrade> newGrade = memberGradeRepository.findGradeForAmount(newTotal);
        if (newGrade.isPresent() && !newGrade.get().getCode().equals(user.getMemberGrade())) {
            user.setMemberGrade(newGrade.get().getCode());
        }

        userService.save(user);
    }
}