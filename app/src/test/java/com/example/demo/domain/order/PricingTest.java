package com.example.demo.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.domain.discount.FullReductionDiscount;
import com.example.demo.domain.dish.DishId;

public class PricingTest {

  /**
   * 商家配置了一个满减规则，订单金额满足条件时正确应用优惠
   */
  @Test
  void order_should_calculate_correct_final_amouont_when_have_a_discount_and_meet_threshold_amount() {
    FullReductionDiscount discount = new FullReductionDiscount(new BigDecimal("50"),
        new BigDecimal("5"));

    OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new BigDecimal("30"));
    List<OrderItem> items = List.of(item1);
    Pricing pricing = Pricing.calculate(items, discount);

    assertThat(pricing.itemsTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    assertThat(pricing.packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
    assertThat(pricing.deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("59.00"));

  }

  /**
   * 商家配置了一个满减规则，订单金额刚好满足条件时正确应用优惠
   */
  @Test
  void order_should_calculate_correct_final_amouont_when_have_a_discount_and_just_meet_threshold_amount() {
    FullReductionDiscount discount = new FullReductionDiscount(new BigDecimal("50"),
        new BigDecimal("5"));

    OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new BigDecimal("25"));
    List<OrderItem> items = List.of(item1);
    Pricing pricing = Pricing.calculate(items, discount);

    assertThat(pricing.itemsTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    assertThat(pricing.packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
    assertThat(pricing.deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("49.00"));
  }

  /**
   * 商家配置了一个满减规则，订单金额不满足条件时不应该应用优惠
   */
  @Test
  void final_amouont_should_no_change_when_have_a_discount_but_not_meet_threshold_amount() {
    FullReductionDiscount discount = new FullReductionDiscount(new BigDecimal("100"),
        new BigDecimal("5"));

    OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new BigDecimal("30"));
    List<OrderItem> items = List.of(item1);
    Pricing pricing = Pricing.calculate(items, discount);

    assertThat(pricing.itemsTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    assertThat(pricing.packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
    assertThat(pricing.deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("64.00"));

  }

  /**
   * 商家未配置规则
   */
  @Test
  void final_amouont_should_no_change_when_have_no_discount() {
    OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new BigDecimal("30"));
    List<OrderItem> items = List.of(item1);
    Pricing pricing = Pricing.calculate(items);

    assertThat(pricing.itemsTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    assertThat(pricing.packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
    assertThat(pricing.deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("64.00"));

  }

  /**
   * 规则配置无效时，订单价格不受影响：
   * 场景1：优惠门槛为负数，订单金额为60，不满足条件，无优惠
   * 场景2：优惠门槛为0，订单金额为60，满足条件，优惠金额为5，最终金额为60 + 1 + 3 - 5 = 59
   * 场景3：优惠金额为负数或者0，门槛为50，订单金额为60，满足条件，但优惠金额无效，不应用优惠
   */
  /*
   * @Test
   * void final_amount_should_no_change_when_have_a_invalid_discount() {
   * // 场景1：优惠门槛为负数，订单金额为60，不满足条件，无优惠
   * FullReductionDiscount discount1 = new FullReductionDiscount(new
   * BigDecimal("-50"),
   * new BigDecimal("5"));
   * OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new
   * BigDecimal("30"));
   * List<OrderItem> items1 = List.of(item1);
   * Pricing pricing1 = Pricing.calculate(items1, discount1);
   * 
   * assertThat(pricing1.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("64.00"));
   * 
   * // 场景2：优惠门槛为0，订单金额为60，满足条件，优惠金额为5，最终金额为60 + 1 + 3 - 5 = 59
   * FullReductionDiscount discount2 = new FullReductionDiscount(BigDecimal.ZERO,
   * new BigDecimal("5"));
   * Pricing pricing2 = Pricing.calculate(items1, discount2);
   * 
   * assertThat(pricing2.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("59.00"));
   * 
   * // 场景3：优惠金额为负数或者0，门槛为50，订单金额为60，满足条件，但优惠金额无效，不应用优惠
   * FullReductionDiscount discount3 = new FullReductionDiscount(new
   * BigDecimal("50"),
   * new BigDecimal("-5"));
   * Pricing pricing3 = Pricing.calculate(items1, discount3);
   * 
   * assertThat(pricing3.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("64.00"));
   * 
   * FullReductionDiscount discount4 = new FullReductionDiscount(new
   * BigDecimal("50"),
   * BigDecimal.ZERO);
   * Pricing pricing4 = Pricing.calculate(items1, discount4);
   * 
   * assertThat(pricing4.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("64.00"));
   * }
   */
  /**
   * 订单响应中应包含折扣信息
   */

  /**
   * 测试当有多个优惠时，能够正确计算最终价格：
   * 场景1：订单金额恰好等于门槛（如50、100），正确触发优惠
   * 场景2：所有优惠门槛都大于订单金额，最终无优惠
   * 场景3：订单金额为60，满足满50减5的条件，但不满足满100减15的条件，因此应该选择满50减5的优惠，
   * 最终金额为60 + 1 + 3 - 5 = 59
   * 场景4：订单金额为120，满足满50减5和满100减15的条件，选择最优惠力度最大的优惠方案，
   * 因此应该选择满100减15的优惠，最终金额为120 + 1 + 3 - 15 = 109
   * 场景5：订单金额为120，满足满50减15和满100减15的条件，此时两种优惠力度相同，选择满50减15的优惠，
   * 最终金额为120 + 1 + 3 - 15 = 109
   * 场景6：订单金额为50，存在多个相同的折扣“满50减10”，只应用一次优惠，最终金额为50 + 1 + 3 - 10 = 44
   */

  @Test
  void should_choose_this_discount_when_only_meet_one_threshold() {

  }

  @Test
  void should_choose_best_discount_when_multiple_meet_threshold() {
    // 场景2：同时满足满50减5和满100减15，选满100减15
  }

  @Test
  void should_choose_lowest_threshold_when_discount_amount_equal() {
    // 场景3：满50减15和满100减15，选满50减15
  }

  /*
   * @Test
   * void should_choose_correct_and_best_discount_when_have_multiple_discounts() {
   * FullReductionDiscount discount1 = new FullReductionDiscount(new
   * BigDecimal("50"),
   * new BigDecimal("5"));
   * FullReductionDiscount discount2 = new FullReductionDiscount(new
   * BigDecimal("100"),
   * new BigDecimal("15"));
   * List<FullReductionDiscount> discounts = List.of(discount1, discount2);
   * 
   * OrderItem item1 = new OrderItem(new DishId("dish1"), "Dish 1", 2, new
   * BigDecimal("30"));
   * List<OrderItem> items = List.of(item1);
   * Pricing pricing1 = Pricing.calculate(items, discounts);
   * 
   * OrderItem item2 = new OrderItem(new DishId("dish1"), "Dish 1", 4, new
   * BigDecimal("30"));
   * List<OrderItem> otheritems = List.of(item2);
   * 
   * Pricing pricing2 = Pricing.calculate(otheritems, discounts);
   * 
   * assertThat(pricing1.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("119.00"));
   * assertThat(pricing2.finalAmount()).isEqualByComparingTo(new
   * BigDecimal("109.00"));
   * }
   */

}
