package com.lera.orders.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@WireMockTest
public abstract class BaseIntegrationTest {
    protected static final PostgreSQLContainer<?> PSQL_CONTAINER;
    protected static final WireMockServer wiremock;
    protected static final int WIREMOCK_PORT = 8199;
    protected static final GenericContainer<?> REDIS_CONTAINER;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    public int serverPort;

    static {
        PSQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");
        PSQL_CONTAINER.start();
        wiremock = new WireMockServer(WIREMOCK_PORT);
        wiremock.start();
        REDIS_CONTAINER = new GenericContainer<>("redis:7-alpine");
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", PSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", PSQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.port = serverPort;
        wiremock.resetAll();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("truncate table orders cascade;");
        jdbcTemplate.execute("truncate table orders_good cascade;");
    }
}
