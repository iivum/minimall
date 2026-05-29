package com.minimall.service;

import com.minimall.dto.MemberBenefitsResponse;
import com.minimall.model.MemberGrade;
import com.minimall.model.User;
import com.minimall.repository.MemberGradeRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberGradeRepository memberGradeRepository;

    @Mock
    private UserService userService;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberGradeRepository, userService);
    }

    @Test
    @DisplayName("getBenefits returns correct benefits for user")
    void getBenefits_returnsCorrectBenefits() {
        User user = new User();
        user.setId("user-1");
        user.setMemberGrade("L2");
        user.setTotalSpent(new BigDecimal("600"));

        MemberGrade currentGrade = new MemberGrade("L2", "白银会员", new BigDecimal("500"), 5, 1);
        MemberGrade nextGrade = new MemberGrade("L3", "黄金会员", new BigDecimal("2000"), 10, 2);

        when(userService.findById("user-1")).thenReturn(user);
        when(memberGradeRepository.findByCode("L2")).thenReturn(Optional.of(currentGrade));
        when(memberGradeRepository.findAll()).thenReturn(List.of(
            new MemberGrade("L1", "普通会员", BigDecimal.ZERO, 0, 1),
            currentGrade,
            nextGrade
        ));

        MemberBenefitsResponse benefits = memberService.getBenefits("user-1");

        assertEquals("L2", benefits.gradeCode());
        assertEquals("白银会员", benefits.gradeName());
        assertEquals(5, benefits.discountPercent());
        assertEquals(1, benefits.pointMultiplier());
        assertEquals(new BigDecimal("600"), benefits.totalSpent());
        assertEquals(new BigDecimal("2000"), benefits.nextGradeThreshold());
        assertEquals("黄金会员", benefits.nextGradeName());
    }

    @Test
    @DisplayName("getBenefits returns no next grade for L5 user")
    void getBenefits_L5User_hasNoNextGrade() {
        User user = new User();
        user.setId("user-1");
        user.setMemberGrade("L5");
        user.setTotalSpent(new BigDecimal("15000"));

        MemberGrade l5Grade = new MemberGrade("L5", "钻石会员", new BigDecimal("10000"), 20, 3);

        when(userService.findById("user-1")).thenReturn(user);
        when(memberGradeRepository.findByCode("L5")).thenReturn(Optional.of(l5Grade));
        when(memberGradeRepository.findAll()).thenReturn(List.of(l5Grade));

        MemberBenefitsResponse benefits = memberService.getBenefits("user-1");

        assertEquals("L5", benefits.gradeCode());
        assertNull(benefits.nextGradeThreshold());
        assertNull(benefits.nextGradeName());
    }

    @Test
    @DisplayName("updateTotalSpent promotes user to higher grade")
    void updateTotalSpent_promotesToHigherGrade() {
        User user = new User();
        user.setId("user-1");
        user.setMemberGrade("L1");
        user.setTotalSpent(new BigDecimal("400"));

        when(userService.findById("user-1")).thenReturn(user);
        when(memberGradeRepository.findGradeForAmount(new BigDecimal("2500"))).thenReturn(
            Optional.of(new MemberGrade("L3", "黄金会员", new BigDecimal("2000"), 10, 2))
        );

        memberService.updateTotalSpent("user-1", new BigDecimal("2100"));

        assertEquals(new BigDecimal("2500"), user.getTotalSpent());
        assertEquals("L3", user.getMemberGrade());
        verify(userService).save(user);
    }

    @Test
    @DisplayName("updateTotalSpent does not demote user")
    void updateTotalSpent_noDemotion() {
        User user = new User();
        user.setId("user-1");
        user.setMemberGrade("L3");
        user.setTotalSpent(new BigDecimal("2000"));

        when(userService.findById("user-1")).thenReturn(user);
        when(memberGradeRepository.findGradeForAmount(new BigDecimal("2500"))).thenReturn(
            Optional.of(new MemberGrade("L3", "黄金会员", new BigDecimal("2000"), 10, 2))
        );

        memberService.updateTotalSpent("user-1", new BigDecimal("500"));

        assertEquals(new BigDecimal("2500"), user.getTotalSpent());
        assertEquals("L3", user.getMemberGrade());
        verify(userService).save(user);
    }

    @Test
    @DisplayName("redeem invokes userService to find user")
    void redeem_callsUserService() {
        User user = new User();
        user.setId("user-1");
        when(userService.findById("user-1")).thenReturn(user);

        memberService.redeem("user-1", "DISCOUNT", new BigDecimal("100"));

        verify(userService).findById("user-1");
    }

    @Test
    @DisplayName("getBenefits throws when grade not found")
    void getBenefits_throwsWhenGradeNotFound() {
        User user = new User();
        user.setId("user-1");
        user.setMemberGrade("INVALID");
        user.setTotalSpent(BigDecimal.ZERO);

        when(userService.findById("user-1")).thenReturn(user);
        when(memberGradeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> memberService.getBenefits("user-1"));
    }
}