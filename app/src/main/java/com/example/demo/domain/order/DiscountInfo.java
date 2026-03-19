package com.example.demo.domain.order;

import java.io.Serializable;
import java.math.BigDecimal;

import com.example.demo.domain.discount.Discount;
import com.example.demo.domain.discount.DiscountType;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record DiscountInfo(
    boolean hasDiscount, // 是否应用了优惠
    String description, // 优惠描述（如"满100减15"）
    BigDecimal amount, // 优惠金额
    @Enumerated(EnumType.STRING) DiscountType type // 优惠类型
) implements Serializable {

  // 无优惠的静态工厂方法
  public static DiscountInfo none() {
    return new DiscountInfo(
        false,
        null,
        BigDecimal.ZERO,
        null);
  }

  // 从Discount创建
  public static DiscountInfo from(Discount discount, BigDecimal amount) {
    return new DiscountInfo(
        true,
        discount.getDescription(),
        amount,
        discount.getType());
  }

  // Getter methods for compatibility and method reference usage
  public boolean applyDiscount() {
    return hasDiscount;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public DiscountType getType() {
    return type;
  }

}
