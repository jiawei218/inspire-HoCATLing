package com.example.demo.domain.order;

import java.math.BigDecimal;

import com.example.demo.domain.discount.Discount;
import com.example.demo.domain.discount.DiscountType;

public record DiscountInfo(
    boolean hasDiscount, // 是否应用了优惠
    String description, // 优惠描述（如"满100减15"）
    BigDecimal amount, // 优惠金额
    DiscountType type // 优惠类型
) {

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

}
