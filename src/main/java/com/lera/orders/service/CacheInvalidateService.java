package com.lera.orders.service;

import com.lera.orders.dto.catalog.kafka.GoodsInvalidateMessage;
import com.lera.orders.dto.catalog.kafka.GoodsItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidateService {

    private final RedisService redisService;

    public void invalidateGoods(GoodsInvalidateMessage message) {
        log.info("Received goods invalidate for {} goods", message.goods().size());

        List<String> externalIds = message.goods().stream()
                .map(GoodsItem::externalId)
                .toList();

        redisService.deleteGoodsFromCache(externalIds);

        log.info("Invalidated cache for externalIds: {}", externalIds);
    }
}
