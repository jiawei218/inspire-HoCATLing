package com.example.demo.domain.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.domain.discount.Discount;

public record Pricing(BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, DiscountInfo discountInfo,
                BigDecimal finalAmount) {
        public static final BigDecimal PACKAGING_FEE = new BigDecimal("1.00");
        public static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

        public static Pricing calculate(List<OrderItem> items) {
                return calculate(items, Optional.empty());
        }

        /**
         * 计算订单价格，考虑折扣的影响
         * 
         * @param items
         * @param discount
         * 
         */
        public static Pricing calculate(List<OrderItem> items, Optional<Discount> discount) {
                BigDecimal itemsTotal = items.stream().map(OrderItem::subtotal).findFirst().orElse(BigDecimal.ZERO);

                BigDecimal discountAmount = discount
                                .map(d -> d.calculateDiscount(itemsTotal).orElse(BigDecimal.ZERO))
                                .orElse(BigDecimal.ZERO);

                BigDecimal finalAmount = itemsTotal.add(PACKAGING_FEE).add(DELIVERY_FEE).subtract(discountAmount);
                finalAmount = finalAmount.max(BigDecimal.ZERO);

                DiscountInfo discountInfo = discount
                                .filter(d -> discountAmount.compareTo(BigDecimal.ZERO) > 0)
                                .map(d -> DiscountInfo.from(d, discountAmount))
                                .orElse(DiscountInfo.none());
                return new Pricing(itemsTotal, PACKAGING_FEE, DELIVERY_FEE, discountInfo, finalAmount);
        }
}
