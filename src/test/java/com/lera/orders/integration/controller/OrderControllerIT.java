package com.lera.orders.integration.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.lera.orders.dto.ConfirmPaymentRequest;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.integration.BaseIntegrationTest;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.model.OrderTestModel;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.DataClassRowMapper;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderControllerIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Проверка создания заказа")
    public void createOrderSuccess() {
        wiremock.stubFor(
                WireMock.post("/catalog/goods/getGoodsList")
                        .willReturn(WireMock.okJson(
                                """
                                                                    {
                                                                      "goods": [
                                                                          {
                                                                            "id": 1,
                                                                            "name": "apple",
                                                                            "description": "green apple",
                                                                            "price": 15.00,
                                                                            "externalId": "a33le"
                                                                          },
                                                                          {
                                                                          "id": 2,
                                                                            "name": "pizza",
                                                                            "description": "tasty pizza",
                                                                            "price": 150.00,
                                                                            "externalId": "pi33a"
                                                                          }
                                                                      ]
                                                                    }
                                        """
                        ))
        );
        //when
        given()
                .contentType(ContentType.JSON)
                .body(
                        new CreateOrderRequest(
                                "123",
                                new BigDecimal("180.00"),
                                List.of(new CreateOrderRequest.GoodDto(
                                                "apple",
                                                new BigDecimal("15.00"),
                                                new BigDecimal("2.00"),
                                                new BigDecimal("30.00"),
                                                "a33le"),
                                        new CreateOrderRequest.GoodDto(
                                                "pizza",
                                                new BigDecimal("150.00"),
                                                new BigDecimal("1.00"),
                                                new BigDecimal("150.00"),
                                                "pi33a"
                                        ))
                        )
                )
                .when()
                .post("/orders/create")
                .then()
                .statusCode(200)
                .body("orderId", equalTo(1));

        //then
        var order = jdbcTemplate.query("select * from orders", new DataClassRowMapper<>(OrderTestModel.class)).getFirst();

        assertThat(order.getOrderId()).isEqualTo(1);
        assertThat(order.getUserId()).isEqualTo("123");
        assertThat(order.getTotalSum()).isEqualTo(new BigDecimal("180.00"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    @DisplayName("Проверка создания заказа, валидация, не проходит validatePrice => ошибка 422, заказ не создается")
    public void createOrderValidationError() {
        wiremock.stubFor(
                WireMock.post("/catalog/goods/getGoodsList")
                        .willReturn(WireMock.okJson(
                                """
                                                                    {
                                                                      "goods": [
                                                                          {
                                                                            "id": 1,
                                                                            "name": "apple",
                                                                            "description": "green apple",
                                                                            "price": 15.00,
                                                                            "externalId": "a33le"
                                                                          },
                                                                          {
                                                                          "id": 2,
                                                                            "name": "pizza",
                                                                            "description": "tasty pizza",
                                                                            "price": 150.00,
                                                                            "externalId": "pi33a"
                                                                          }
                                                                      ]
                                                                    }
                                        """
                        ))
        );
        //when
        given()
                .contentType(ContentType.JSON)
                .body(
                        new CreateOrderRequest(
                                "123",
                                new BigDecimal("170.00"),
                                List.of(new CreateOrderRequest.GoodDto(
                                                "apple",
                                                new BigDecimal("10.00"),
                                                new BigDecimal("2.00"),
                                                new BigDecimal("20.00"),
                                                "a33le"),
                                        new CreateOrderRequest.GoodDto(
                                                "pizza",
                                                new BigDecimal("150.00"),
                                                new BigDecimal("1.00"),
                                                new BigDecimal("150.00"),
                                                "pi33a"
                                        ))
                        )
                )
                .when()
                .post("/orders/create")
                .then()
                .statusCode(422);

        //then
        var orders = jdbcTemplate.query("select * from orders", new DataClassRowMapper<>(OrderTestModel.class));

        assertThat(orders.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Проверка подтверждения оплаты")
    public void confirmPaymentSuccess() {
        jdbcTemplate.execute(
                "insert into orders (order_id, user_id, total_sum, status, payment_id) values (1, '4sus', 150.00, 'NEW', NULL)"
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .body(
                        new ConfirmPaymentRequest(
                                1L,
                                1111L,
                                new BigDecimal("150.00")
                        )
                )
                .when()
                .post("/orders/confirmPayment")
                .then()
                .statusCode(200);

        //then
        var order = jdbcTemplate.query("select * from orders", new DataClassRowMapper<>(OrderTestModel.class)).getFirst();

        assertThat(order.getStatus().equals(OrderStatus.PAID));
        assertThat(order.getPaymentId().equals(1111L));
    }

    @Test
    @DisplayName("Проверка подтверждения оплаты, валидация, не проходит validateSum => ошибка 422, данные о заказе не обновляются")
    public void confirmPaymentValidationError() {
        jdbcTemplate.execute(
                "insert into orders (order_id, user_id, total_sum, status, payment_id) values (1, '4sus', 150.00, 'NEW', NULL)"
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .body(
                        new ConfirmPaymentRequest(
                                1L,
                                1111L,
                                new BigDecimal("100.00")
                        )
                )
                .when()
                .post("/orders/confirmPayment")
                .then()
                .statusCode(422);

        //then
        var order = jdbcTemplate.query("select * from orders", new DataClassRowMapper<>(OrderTestModel.class)).getFirst();

        assertThat(order.getStatus().equals(OrderStatus.NEW));
        assertNull(order.getPaymentId());
    }
}
