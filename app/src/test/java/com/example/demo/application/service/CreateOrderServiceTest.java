package com.example.demo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.demo.application.service.CreateOrderService.CreateOrderCommand;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand.DeliveryInfoDto;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand.DiscountParaDto;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand.OrderItemDto;
import com.example.demo.domain.discount.DiscountType;

import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderRepository;
import com.example.demo.domain.order.Pricing;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    CreateOrderService createOrderService;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    @Test
    void create_order_should_create_and_save_order() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderCommand command = new CreateOrderCommand("user-001", "merchant-001", List.of(item), deliveryInfo,
                "少辣", null);

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getUserId().value()).isEqualTo("user-001");
        assertThat(capturedOrder.getMerchantId().value()).isEqualTo("merchant-001");
        assertThat(capturedOrder.getItems()).hasSize(1);
        assertThat(capturedOrder.getItems().get(0).dishId().value()).isEqualTo("dish-001");
        assertThat(capturedOrder.getItems().get(0).dishName()).isEqualTo("宫保鸡丁");
        assertThat(capturedOrder.getItems().get(0).quantity()).isEqualTo(2);
        assertThat(capturedOrder.getItems().get(0).price()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(capturedOrder.getDeliveryInfo().recipientName()).isEqualTo("张三");
        assertThat(capturedOrder.getDeliveryInfo().recipientPhone()).isEqualTo("13800138000");
        assertThat(capturedOrder.getDeliveryInfo().address()).isEqualTo("北京市朝阳区某某街道123号");
        assertThat(capturedOrder.getPricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(capturedOrder.getPricing().packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
        assertThat(capturedOrder.getPricing().deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
        assertThat(capturedOrder.getPricing().finalAmount()).isEqualByComparingTo(new BigDecimal("54.00"));
        assertThat(capturedOrder.getRemark()).isEqualTo("少辣");
    }

    @Test
    void create_order_should_set_status_to_pending_payment() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 1, new BigDecimal("25.00"));
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderCommand command = new CreateOrderCommand("user-001", "merchant-001", List.of(item), deliveryInfo,
                null, null);

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getStatus().name()).isEqualTo("PENDING_PAYMENT");
    }

    @Test
    void create_order_meet_discount_should_apply_discount() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 4, new BigDecimal("30.00")); // 总价120
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");
        DiscountParaDto discountPara = new DiscountParaDto(DiscountType.FULL_REDUCTION, new BigDecimal("100.00"),
                new BigDecimal("15.00"));
        // 构造满100减15的折扣

        CreateOrderCommand command = new CreateOrderCommand(
                "user-001", "merchant-001", List.of(item),
                deliveryInfo, "备注", discountPara);

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getPricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(capturedOrder.getPricing().discountInfo().applyDiscount()).isTrue();
        assertThat(capturedOrder.getPricing().discountInfo().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(capturedOrder.getPricing().discountInfo().getType()).isEqualTo(DiscountType.FULL_REDUCTION);
        assertThat(capturedOrder.getPricing().finalAmount()).isEqualByComparingTo(new BigDecimal("109.00")); // 120+1+3-15
    }

    @Test
    void create_order_with_discount_condition_not_met_should_not_apply_discount() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("40.00")); // 总价80
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");
        DiscountParaDto discountParam = new DiscountParaDto(
                DiscountType.FULL_REDUCTION,
                new BigDecimal("100.00"), // 门槛100
                new BigDecimal("15.00"));

        CreateOrderCommand command = new CreateOrderCommand(
                "user-001", "merchant-001", List.of(item), deliveryInfo, "备注", discountParam);

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getPricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(capturedOrder.getPricing().discountInfo().applyDiscount()).isFalse();
        assertThat(capturedOrder.getPricing().discountInfo().getType()).isEqualTo(null);
        assertThat(capturedOrder.getPricing().discountInfo().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedOrder.getPricing().finalAmount()).isEqualByComparingTo(new BigDecimal("84.00")); // 80+1+3
    }

    @Test
    void create_order_should_validate_discount_param() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("40.00"));
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");
        // 非法折扣参数：门槛为负数
        DiscountParaDto invalidDiscount = new DiscountParaDto(
                DiscountType.FULL_REDUCTION,
                new BigDecimal("-100.00"),
                new BigDecimal("15.00"));
        CreateOrderCommand command = new CreateOrderCommand(
                "user-001", "merchant-001", List.of(item), deliveryInfo, "备注", invalidDiscount);
        assertThatThrownBy(() -> createOrderService.createOrder(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Threshold cannot be negative");
    }

    @Test
    void create_order_should_return_discount_info_in_result() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 4, new BigDecimal("30.00")); // 总价120
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");
        DiscountParaDto discountParam = new DiscountParaDto(
                DiscountType.FULL_REDUCTION,
                new BigDecimal("100.00"),
                new BigDecimal("15.00"));
        CreateOrderCommand command = new CreateOrderCommand(
                "user-001", "merchant-001", List.of(item), deliveryInfo, "备注", discountParam);
        CreateOrderService.CreateOrderResult result = createOrderService.createOrder(command);
        assertThat(result.pricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(result.pricing().finalAmount()).isEqualByComparingTo(new BigDecimal("109.00")); // 120+1+3-15
        // 假设 pricing() 有 discountInfo 字段
        assertThat(result.pricing().discountInfo().hasDiscount()).isTrue();
        assertThat(result.pricing().discountInfo().amount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.pricing().discountInfo().type()).isEqualTo(DiscountType.FULL_REDUCTION);
    }

    @Test
    void create_order_without_discount_should_not_apply_discount() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("40.00")); // 总价80
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderCommand command = new CreateOrderCommand(
                "user-001", "merchant-001", List.of(item), deliveryInfo, "备注", null);

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getPricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(capturedOrder.getPricing().discountInfo().applyDiscount()).isFalse();
        assertThat(capturedOrder.getPricing().discountInfo().getType()).isEqualTo(null);
        assertThat(capturedOrder.getPricing().discountInfo().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedOrder.getPricing().finalAmount()).isEqualByComparingTo(new BigDecimal("84.00")); // 80+1+3
    }

}
