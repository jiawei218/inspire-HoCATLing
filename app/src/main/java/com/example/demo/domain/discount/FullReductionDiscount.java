package com.example.demo.domain.discount;

import java.math.BigDecimal;
import java.util.Optional;

public record FullReductionDiscount(BigDecimal threshold, BigDecimal discountAmount) implements Discount {

  public FullReductionDiscount {
    if (threshold == null) {
      throw new IllegalArgumentException("Threshold cannot be null");
    }
    if (discountAmount == null) {
      throw new IllegalArgumentException("Discount amount cannot be null");
    }
    if (threshold.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Threshold cannot be negative");
    }
    if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Discount amount cannot be negative");
    }
    if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalArgumentException("Discount amount cannot be zero");
    }
  }

  public BigDecimal getThreshold() {
    return threshold;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  @Override
  public DiscountType getType() {
    return DiscountType.FULL_REDUCTION;
  }

  @Override
  public String getDescription() {
    return String.format("满%s减%s", threshold.stripTrailingZeros().toPlainString(),
        discountAmount.stripTrailingZeros().toPlainString());
  }

  @Override
  public Optional<BigDecimal> calculateDiscount(BigDecimal itemsTotal) {
    // 满减的计算逻辑，根据实际需求实现
    // 例如：满100减20，满200减50等
    if (itemsTotal.compareTo(threshold) >= 0) {
      return Optional.of(discountAmount);
    }
    return Optional.empty();
  }

}
