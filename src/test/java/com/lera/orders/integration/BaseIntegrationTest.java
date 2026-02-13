package com.lera.orders.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BaseIntegrationTest {
    protected static final PostgreSQLContainer<?> PSQL_CONTAINER;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    public int serverPort;

    static {
        PSQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");
        PSQL_CONTAINER.start();
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
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("truncate table good cascade;");
    }
}
