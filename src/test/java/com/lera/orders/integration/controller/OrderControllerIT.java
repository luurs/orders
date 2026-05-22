package com.lera.orders.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.lera.orders.dto.ConfirmPaymentRequest;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import com.lera.orders.integration.BaseIntegrationTest;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.model.OrderTestModel;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.DataClassRowMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderControllerIT extends BaseIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

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
                .body("orderId", notNullValue());

        //then
        var order = jdbcTemplate.query("select * from orders", new DataClassRowMapper<>(OrderTestModel.class)).getFirst();

        assertThat(order.getOrderId()).isPositive();
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
    @DisplayName("кэш работает")
    public void cacheSuccess() {
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
        // when
        var request = new CreateOrderRequest(
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
        );
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/orders/create")
                .then()
                .statusCode(200);
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/orders/create")
                .then()
                .statusCode(200);
        // then
        wiremock.verify(1, WireMock.postRequestedFor(
                WireMock.urlEqualTo("/catalog/goods/getGoodsList")));
    }

    @Test
    @DisplayName("кэш работает частично. Один товар есть в redis, второго нет")
    public void cacheNotFull() throws JsonProcessingException {
        var cachedGood = new GetGoodsListResponse.GoodDto(
                1L,
                "apple",
                "green apple",
                new BigDecimal("15.00"),
                "a33le");
        redisTemplate.opsForValue().set("catalog:good:a33le", objectMapper.writeValueAsString(cachedGood));

        wiremock.stubFor(
                WireMock.post("/catalog/goods/getGoodsList")
                        .willReturn(WireMock.okJson(
                                """
                                                                    {
                                                                      "goods": [
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
        // when
        var request = new CreateOrderRequest(
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
        );
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/orders/create")
                .then()
                .statusCode(200);
        // then
        wiremock.verify(1, WireMock.postRequestedFor(
                WireMock.urlEqualTo("/catalog/goods/getGoodsList"))
                .withRequestBody(WireMock.containing("pi33a"))
                .withRequestBody(WireMock.notContaining("a33le")));
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

    @Test
    @DisplayName("кафка должна инвалидировать кэш (удалять товар при его изменении в catalog)")
    public void kafkaMessageInvalidateCache() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        var apple = new GetGoodsListResponse.GoodDto(1L, "apple", "green apple", new BigDecimal("15.00"), "a33le");
        var pizza = new GetGoodsListResponse.GoodDto(2L, "pizza", "tasty pizza", new BigDecimal("150.00"), "pi33a");
        redisTemplate.opsForValue().set("catalog:good:a33le", objectMapper.writeValueAsString(apple));
        redisTemplate.opsForValue().set("catalog:good:pi33a", objectMapper.writeValueAsString(pizza));

        assertThat(redisTemplate.hasKey("catalog:good:a33le")).isTrue();
        assertThat(redisTemplate.hasKey("catalog:good:pi33a")).isTrue();

        //when
        try (var produser = createTestProducer()) {
            String message = """
                    {"goods": [
                            {
                                "id":1,
                                "externalId":"a33le"
                            },
                            {
                                "id":2,
                                "externalId":"pi33a"
                            }
                        ]
                    }
                    """;
            produser.send(new ProducerRecord<>("catalog.invalidate-goods-cache", message)).get();
        }
        //then
        await().atMost(10, SECONDS).untilAsserted(() -> {
            assertThat(redisTemplate.hasKey("catalog:good:a33le")).isFalse();
            assertThat(redisTemplate.hasKey("catalog:good:pi33a")).isFalse();
        });
    }

    private KafkaProducer<String, String> createTestProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }
}
