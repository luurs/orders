package com.lera.orders.service;

import com.lera.orders.clients.CatalogClient;
import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CatalogCacheService {

    private final CatalogClient catalogClient;
    private final RedisService redisService;

    public GetGoodsListResponse getGoods(List<String> externalIds) {
        var cachedGoods = redisService.getGoodsFromCache(externalIds);

        if (cachedGoods.size() == externalIds.size()) {
            return new GetGoodsListResponse(cachedGoods);
        }

        var cachedGoodsIds = cachedGoods.stream()
                .map(GetGoodsListResponse.GoodDto::externalId)
                .collect(Collectors.toSet());
        var nonCachedGoodsIds = new HashSet<>(externalIds).stream()
                .filter(id -> !cachedGoodsIds.contains(id))
                .toList();

        var goodsFromCatalog = catalogClient.getGoodsList(new GetGoodsListRequest(nonCachedGoodsIds)).goods();
        redisService.saveGoodsToCache(goodsFromCatalog);

        var allGoods = Stream.concat(cachedGoods.stream(), goodsFromCatalog.stream()).toList();
        return new GetGoodsListResponse(allGoods);
    }
}
