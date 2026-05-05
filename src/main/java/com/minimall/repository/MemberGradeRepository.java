package com.minimall.repository;

import com.minimall.model.MemberGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface MemberGradeRepository extends JpaRepository<MemberGrade, String> {
    Optional<MemberGrade> findByCode(String code);

    @Query("SELECT mg FROM MemberGrade mg WHERE mg.minAmount <= :amount ORDER BY mg.minAmount DESC LIMIT 1")
    Optional<MemberGrade> findGradeForAmount(@Param("amount") BigDecimal amount);
}