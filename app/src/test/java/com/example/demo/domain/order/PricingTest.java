package com.example.demo.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.domain.discount.FullReductionDiscount;
import com.example.demo.domain.dish.DishId;

public class PricingTest {

  @Test
  void order_should_calculate_correct_price_when_have_a_discount_and_meet_threshold_amount() {
    FullReductionDiscount discount = new FullReductionDiscount(new BigDecimal("50"),
        new BigDecimal("5"));

    OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new BigDecimal("30"));
    List<OrderItem> items = List.of(item1);
    Pricing pricing = Pricing.calculate(items, discount);

    assertThat(pricing.itemsTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    assertThat(pricing.packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
    assertThat(pricing.deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("59.00"));

    // assertThat(pricing.discountInfo().applied()).isEqualTo(true);
    // assertThat(pricing.discountInfo().discountType()).isEqualTo("满减");
    // assertThat(pricing.discountInfo().amount()).isEqualByComparingTo(new
    // BigDecimal("5.00"));
  }

}
