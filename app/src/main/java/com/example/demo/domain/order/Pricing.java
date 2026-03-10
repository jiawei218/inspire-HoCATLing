package com.example.demo.domain.order;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.domain.discount.Discount;

public record Pricing(BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, DiscountInfo discountInfo,
        BigDecimal finalAmount) {
    public static final BigDecimal PACKAGING_FEE = new BigDecimal("1.00");
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

    public static Pricing calculate(List<OrderItem> items) {
        BigDecimal itemsTotal = items.stream().map(OrderItem::subtotal).findFirst().orElse(BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal finalAmount = itemsTotal.add(PACKAGING_FEE).add(DELIVERY_FEE)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        DiscountInfo discountInfo = DiscountInfo.none();
        return new Pricing(itemsTotal, PACKAGING_FEE, DELIVERY_FEE, discountInfo, finalAmount);
    }

    /**
     * 计算订单价格，考虑折扣的影响
     * 
     * @param items
     * @param discount
     * 
     */
    public static Pricing calculate(List<OrderItem> items, Discount discount) {
        BigDecimal itemsTotal = items.stream().map(OrderItem::subtotal).findFirst().orElse(BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal discountAmount = discount.calculateDiscount(itemsTotal).orElse(BigDecimal.ZERO).setScale(2,
                java.math.RoundingMode.HALF_UP);

        BigDecimal finalAmount = itemsTotal.add(PACKAGING_FEE).add(DELIVERY_FEE).subtract(discountAmount);
        finalAmount = finalAmount.max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);

        DiscountInfo discountInfo = discountAmount.compareTo(BigDecimal.ZERO) > 0
                ? DiscountInfo.from(discount, discountAmount)
                : DiscountInfo.none();
        return new Pricing(itemsTotal, PACKAGING_FEE, DELIVERY_FEE, discountInfo, finalAmount);
    }
}
