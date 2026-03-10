package com.example.demo.domain.discount;

import java.math.BigDecimal;
import java.util.Optional;

public interface Discount {
  DiscountType getType();

  String getDescription();

  /**
   * 计算可应用的优惠金额
   * 
   * @param itemsTotal 商品总价（不含包装费、配送费）
   * @return 如果满足条件返回优惠金额，否则返回Optional.empty()
   */
  Optional<BigDecimal> calculateDiscount(BigDecimal itemsTotal);

}
