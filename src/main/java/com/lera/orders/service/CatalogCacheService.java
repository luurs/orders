package com.lera.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lera.orders.clients.CatalogClient;
import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogCacheService {

    @Value("${cache.catalog.good.ttl}")
    private Duration ttl;
    private final CatalogClient catalogClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public GetGoodsListResponse getGoods(List<String> externalIds) {
        List<GetGoodsListResponse.GoodDto> foundGoods = new ArrayList<>();
        List<String> nonCachedGoodsIds = new ArrayList<>();

        getFromCatalog(externalIds, foundGoods, nonCachedGoodsIds);

        if (!nonCachedGoodsIds.isEmpty()) {
            putIntoCache(nonCachedGoodsIds, foundGoods);
        }

        return new GetGoodsListResponse(foundGoods);
    }

    private void getFromCatalog(List<String> externalIds, List<GetGoodsListResponse.GoodDto> foundGoods, List<String> nonCachedGoodsIds) {
        List<String> keys = externalIds.stream()
                .map(id -> "catalog:good:" + id)
                .toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        for (int i = 0; i < externalIds.size(); i++) {
            String cached = values.get(i);
            if (cached != null) {
                try {
                    foundGoods.add(objectMapper.readValue(cached, GetGoodsListResponse.GoodDto.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                nonCachedGoodsIds.add(externalIds.get(i));
            }
        }
    }

    private void putIntoCache(List<String> nonCachedGoodsIds, List<GetGoodsListResponse.GoodDto> foundGoods) {
        GetGoodsListResponse fromCatalog = catalogClient.getGoodsList(new GetGoodsListRequest(nonCachedGoodsIds));

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            fromCatalog.goods().forEach(good -> {
                try {
                    byte[] key = ("catalog:good:" + good.externalId()).getBytes();
                    byte[] value = objectMapper.writeValueAsBytes(good);
                    connection.stringCommands().set(key, value,
                            Expiration.from(ttl), RedisStringCommands.SetOption.UPSERT);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        });

        foundGoods.addAll(fromCatalog.goods());
    }
}
