package com.minimall.config;

import com.minimall.model.MemberGrade;
import com.minimall.repository.MemberGradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class MemberGradeInitializer implements CommandLineRunner {
    private final MemberGradeRepository memberGradeRepository;

    public MemberGradeInitializer(MemberGradeRepository memberGradeRepository) {
        this.memberGradeRepository = memberGradeRepository;
    }

    @Override
    public void run(String... args) {
        if (memberGradeRepository.count() == 0) {
            List<MemberGrade> grades = List.of(
                new MemberGrade("L1", "普通会员", BigDecimal.ZERO, 0, 1),
                new MemberGrade("L2", "白银会员", new BigDecimal("500"), 5, 1),
                new MemberGrade("L3", "黄金会员", new BigDecimal("2000"), 10, 2),
                new MemberGrade("L4", "铂金会员", new BigDecimal("5000"), 15, 2),
                new MemberGrade("L5", "钻石会员", new BigDecimal("10000"), 20, 3)
            );
            memberGradeRepository.saveAll(grades);
        }
    }
}