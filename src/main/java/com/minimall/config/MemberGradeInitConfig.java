package com.minimall.config;

import com.minimall.model.MemberGrade;
import com.minimall.repository.MemberGradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class MemberGradeInitConfig {

    @Bean
    CommandLineRunner initMemberGrades(MemberGradeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(createGrade("L1", "普通会员", BigDecimal.ZERO, 0, BigDecimal.ONE));
                repository.save(createGrade("L2", "青铜会员", new BigDecimal("500"), 5, new BigDecimal("1.2")));
                repository.save(createGrade("L3", "白银会员", new BigDecimal("2000"), 10, new BigDecimal("1.5")));
                repository.save(createGrade("L4", "黄金会员", new BigDecimal("5000"), 15, new BigDecimal("2.0")));
                repository.save(createGrade("L5", "钻石会员", new BigDecimal("10000"), 20, new BigDecimal("3.0")));
            }
        };
    }

    private MemberGrade createGrade(String code, String name, BigDecimal minAmount, int discountPercent, BigDecimal pointMultiplier) {
        MemberGrade grade = new MemberGrade();
        grade.setCode(code);
        grade.setName(name);
        grade.setMinAmount(minAmount);
        grade.setDiscountPercent(discountPercent);
        grade.setPointMultiplier(pointMultiplier);
        return grade;
    }
}