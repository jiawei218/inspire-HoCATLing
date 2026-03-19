package com.example.demo.web.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.discount.DiscountType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreateOrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void create_order_should_succeed_with_valid_request() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = createValidOrderRequest();

                // When & Then
                MvcResult result = mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code").value(0))
                                .andExpect(jsonPath("$.message").value("订单创建成功"))
                                .andExpect(jsonPath("$.data.orderId").exists())
                                .andExpect(jsonPath("$.data.orderNumber").exists())
                                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                                .andExpect(jsonPath("$.data.pricing.itemsTotal").value(50.00))
                                .andExpect(jsonPath("$.data.pricing.packagingFee").value(1.00))
                                .andExpect(jsonPath("$.data.pricing.deliveryFee").value(3.00))
                                .andExpect(jsonPath("$.data.pricing.finalAmount").value(54.00))
                                .andExpect(jsonPath("$.data.createdAt").exists())
                                .andReturn();

                // Verify order number format (yyyyMMddHHmmss + 6 digits)
                String responseBody = result.getResponse().getContentAsString();
                String orderNumber = objectMapper
                                .readTree(responseBody)
                                .path("data")
                                .path("orderNumber")
                                .asText();
                assertThat(orderNumber).matches("\\d{20}");
        }

        @Test
        void create_order_should_reject_request_with_missing_merchant_id() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                null, // missing merchantId
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_missing_items() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                null, // missing items
                                createValidDeliveryInfo(),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_empty_items() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(), // empty items
                                createValidDeliveryInfo(),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_missing_delivery_info() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                null, // missing deliveryInfo
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_invalid_phone_number() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest(
                                                "张三",
                                                "12345678901", // invalid phone format
                                                "北京市朝阳区xxx街道xxx号"),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_too_long_remark() throws Exception {
                // Given
                String longRemark = "a".repeat(201); // 201 characters
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001", List.of(createValidOrderItem()), createValidDeliveryInfo(), longRemark,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_too_long_address() throws Exception {
                // Given
                String longAddress = "a".repeat(501); // 501 characters
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest("张三", "13800138000",
                                                longAddress),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_invalid_quantity() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                                                "dish-001",
                                                "宫保鸡丁",
                                                0, // invalid quantity
                                                new BigDecimal("25.00"))),
                                createValidDeliveryInfo(),
                                null,
                                null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_succeed_with_remark() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001", List.of(createValidOrderItem()), createValidDeliveryInfo(), "少辣", null);

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code").value(0))
                                .andExpect(jsonPath("$.data.orderId").exists());
        }

        @Test
        void create_order_should_succeed_with_valid_discount() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                "FULL_REDUCTION",
                                                new BigDecimal("50.00"),
                                                new BigDecimal("20.00")));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code").value(0))
                                .andExpect(jsonPath("$.data.orderId").exists())
                                .andExpect(jsonPath("$.data.pricing.finalAmount").value(34.00))
                                .andExpect(jsonPath("$.data.pricing.discountData.hasDiscount").value(true))
                                .andExpect(jsonPath("$.data.pricing.discountData.type")
                                                .value(DiscountType.FULL_REDUCTION.name()))
                                .andExpect(jsonPath("$.data.pricing.discountData.description").value("满50减20"))
                                .andExpect(jsonPath("$.data.pricing.discountData.amount").value(20.00)); // 50 - 20

        }

        @Test
        void create_order_should_succeed_without_discount() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                null); // no discount

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code").value(0))
                                .andExpect(jsonPath("$.data.orderId").exists())
                                .andExpect(jsonPath("$.data.pricing.finalAmount").value(54.00))
                                .andExpect(jsonPath("$.data.pricing.discountData.hasDiscount").value(false))
                                .andExpect(jsonPath("$.data.pricing.discountData.type")
                                                .value(""))
                                .andExpect(jsonPath("$.data.pricing.discountData.description").value(""))
                                .andExpect(jsonPath("$.data.pricing.discountData.amount").value(0.00));

        }

        @Test
        void create_order_should_reject_request_with_missing_discount_type() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                null, // missing discount type
                                                new BigDecimal("100.00"),
                                                new BigDecimal("20.00")));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_missing_discount_threshold() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                "FULL_REDUCTION",
                                                null, // missing discount threshold
                                                new BigDecimal("20.00")));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_missing_discount_amount() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                "FULL_REDUCTION",
                                                new BigDecimal("50.00"),
                                                null));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        @Test
        void create_order_should_reject_request_with_invalid_discount_threshold() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                "FULL_REDUCTION",
                                                new BigDecimal("-100.00"), // invalid negative threshold
                                                new BigDecimal("20.00")));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));

        }

        @Test
        void create_order_should_reject_request_with_invalid_discount_amount() throws Exception {
                // Given
                CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                                "merchant-001",
                                List.of(createValidOrderItem()),
                                createValidDeliveryInfo(),
                                null,
                                new CreateOrderController.CreateOrderRequest.DiscountParaRequest(
                                                "FULL_REDUCTION",
                                                new BigDecimal("100.00"), // invalid negative threshold
                                                new BigDecimal("0.00")));

                // When & Then
                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user("test-user-id")))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("ValidationError"));
        }

        private CreateOrderController.CreateOrderRequest createValidOrderRequest() {
                return new CreateOrderController.CreateOrderRequest(
                                "merchant-001", List.of(createValidOrderItem()), createValidDeliveryInfo(), null, null);
        }

        private CreateOrderController.CreateOrderRequest.OrderItemRequest createValidOrderItem() {
                return new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                                "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        }

        private CreateOrderController.CreateOrderRequest.DeliveryInfoRequest createValidDeliveryInfo() {
                return new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest("张三", "13800138000",
                                "北京市朝阳区xxx街道xxx号");
        }
}
