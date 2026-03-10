package com.example.demo.infrastructure.persistence.order.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

import com.example.demo.domain.order.DiscountInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingEmbeddable {
    private BigDecimal itemsTotal;
    private BigDecimal packagingFee;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;

    public DiscountInfo getDiscountInfo() {
        // 订单价格中不直接存储优惠信息，返回一个默认的无优惠信息
        return DiscountInfo.none();
    }
}
