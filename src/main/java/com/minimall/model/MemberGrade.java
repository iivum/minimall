package com.minimall.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "member_grades")
public class MemberGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "point_multiplier", nullable = false)
    private BigDecimal pointMultiplier;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getPointMultiplier() { return pointMultiplier; }
    public void setPointMultiplier(BigDecimal pointMultiplier) { this.pointMultiplier = pointMultiplier; }
}