package com.lera.orders.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@WireMockTest
public abstract class BaseIntegrationTest {
    protected static final PostgreSQLContainer<?> PSQL_CONTAINER;
    protected static final WireMockServer wiremock;
    protected static final int WIREMOCK_PORT = 8199;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    public int serverPort;

    static {
        PSQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");
        PSQL_CONTAINER.start();
        wiremock = new WireMockServer(WIREMOCK_PORT);
        wiremock.start();
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
