package com.minimall.repository;

import com.minimall.model.MemberGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MemberGradeRepository extends JpaRepository<MemberGrade, String> {
    Optional<MemberGrade> findByCode(String code);

    @Query("SELECT g FROM MemberGrade g WHERE :amount >= g.minAmount ORDER BY g.minAmount DESC LIMIT 1")
    Optional<MemberGrade> findGradeForAmount(@Param("amount") java.math.BigDecimal amount);
}