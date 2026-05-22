package com.lera.orders.consumer;

import com.lera.orders.dto.catalog.kafka.GoodsInvalidateMessage;
import com.lera.orders.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoodsInvalidateConsumer {

    private final RedisService redisService;

    @KafkaListener(topics = "${catalog.kafka.topics.invalidate-goods-cache}")
    public void handle(GoodsInvalidateMessage message) {
        log.info("Received goods invalidate for {} goods", message.goods().size());

        List<String> externalIds = message.goods().stream()
                .map(item -> item.externalId())
                .toList();

        redisService.deleteGoodsFromCache(externalIds);

        log.info("Invalidated cache for externalIds: {}", externalIds);
    }

}
