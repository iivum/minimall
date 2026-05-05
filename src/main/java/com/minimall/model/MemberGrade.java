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
    private Integer pointMultiplier;

    public MemberGrade() {}

    public MemberGrade(String code, String name, BigDecimal minAmount, Integer discountPercent, Integer pointMultiplier) {
        this.code = code;
        this.name = name;
        this.minAmount = minAmount;
        this.discountPercent = discountPercent;
        this.pointMultiplier = pointMultiplier;
    }

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
    public Integer getPointMultiplier() { return pointMultiplier; }
    public void setPointMultiplier(Integer pointMultiplier) { this.pointMultiplier = pointMultiplier; }
}