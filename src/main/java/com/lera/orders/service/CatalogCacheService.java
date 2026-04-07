package com.lera.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lera.orders.clients.CatalogClient;
import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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
        List<String> missing = new ArrayList<>();

        for (String externalId : externalIds) {
            String key = "catalog:good:" + externalId;
            String cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                GetGoodsListResponse.GoodDto dto = null;
                try {
                    dto = objectMapper.readValue(cached, GetGoodsListResponse.GoodDto.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                foundGoods.add(dto);
            } else {
                missing.add(externalId);
            }
        }

        if (!missing.isEmpty()) {
            GetGoodsListResponse fromCatalog = catalogClient.getGoodsList(new GetGoodsListRequest(missing));

            for (GetGoodsListResponse.GoodDto good : fromCatalog.goods()) {
                String key = "catalog:good:" + good.externalId();
                String json = null;
                try {
                    json = objectMapper.writeValueAsString(good);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                redisTemplate.opsForValue().set(key, json, ttl);
            }

            foundGoods.addAll(fromCatalog.goods());
        }

        return new GetGoodsListResponse(foundGoods);
    }
}
