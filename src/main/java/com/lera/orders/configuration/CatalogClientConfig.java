package com.lera.orders.configuration;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CatalogClientConfig {

    // вообще можно удалить бин чтобы не дублировать проперти
    @Bean
    public Request.Options catalogRequestOptions() {
        return new Request.Options(
                2000, TimeUnit.MILLISECONDS,
                5000, TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public Retryer catalogRetryer() {
        return new Retryer.Default(
                100,
                1000,
                3
        );
    }
}
