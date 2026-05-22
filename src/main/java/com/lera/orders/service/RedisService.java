package com.lera.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${cache.catalog.good.ttl}")
    private Duration ttl;

    public List<GetGoodsListResponse.GoodDto> getGoodsFromCache(List<String> externalIds) {
        List<String> keys = externalIds.stream()
                .map(id -> "catalog:good:" + id)
                .toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) return Collections.emptyList();
        return values.stream()
                .filter(Objects::nonNull)
                .map(value -> {
                    try {
                        return objectMapper.readValue(value, GetGoodsListResponse.GoodDto.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public void saveGoodsToCache(List<GetGoodsListResponse.GoodDto> goods) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            goods.forEach(good -> {
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
    }

    public void deleteGoodsFromCache(List<String> externalIds) {
        List<String> keys = externalIds.stream()
                .map(id -> "catalog:good:" + id)
                .toList();
        redisTemplate.delete(keys);
    }
}
