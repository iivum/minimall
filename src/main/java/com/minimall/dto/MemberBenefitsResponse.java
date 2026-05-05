package com.minimall.dto;

import com.minimall.model.MemberGrade;
import com.minimall.model.User;

public record MemberBenefitsResponse(
    String gradeCode,
    String gradeName,
    Integer discountPercent,
    Integer pointMultiplier,
    java.math.BigDecimal totalSpent,
    java.math.BigDecimal nextGradeThreshold,
    String nextGradeName
) {
    public static MemberBenefitsResponse from(User user, MemberGrade grade, MemberGrade nextGrade) {
        return new MemberBenefitsResponse(
            grade.getCode(),
            grade.getName(),
            grade.getDiscountPercent(),
            grade.getPointMultiplier(),
            user.getTotalSpent(),
            nextGrade != null ? nextGrade.getMinAmount() : null,
            nextGrade != null ? nextGrade.getName() : null
        );
    }
}