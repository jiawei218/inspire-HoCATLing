package com.example.demo.domain.discount;

import java.math.BigDecimal;
import java.util.Optional;

public record FullReductionDiscount(BigDecimal threshold, BigDecimal discountAmount) implements Discount {

  @Override
  public DiscountType getType() {
    return DiscountType.FULL_REDUCTION;
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
