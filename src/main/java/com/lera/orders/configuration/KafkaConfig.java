package com.lera.orders.configuration;

import com.lera.orders.dto.catalog.kafka.GoodsInvalidateMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, GoodsInvalidateMessage> goodsInvalidateConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(GoodsInvalidateMessage.class, false)
        );
    }

    // Контейнер для листенера
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GoodsInvalidateMessage> goodsInvalidateContainerFactory(
            ConsumerFactory<String, GoodsInvalidateMessage> goodsInvalidateConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, GoodsInvalidateMessage>();
        factory.setConsumerFactory(goodsInvalidateConsumerFactory);
        return factory;
    }
}
