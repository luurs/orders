package com.lera.orders.consumer;

import com.lera.orders.dto.catalog.kafka.GoodsInvalidateMessage;
import com.lera.orders.service.CacheInvalidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoodsInvalidateConsumer {

    private final CacheInvalidateService cacheInvalidateService;

    @KafkaListener(
            topics = "${catalog.kafka.topics.invalidate-goods-cache}",
            containerFactory = "goodsInvalidateContainerFactory"
    )
    public void handle(GoodsInvalidateMessage message) {
        cacheInvalidateService.invalidateGoods(message);
    }

}
