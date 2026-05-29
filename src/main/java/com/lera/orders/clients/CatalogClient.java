package com.lera.orders.clients;

import com.lera.orders.configuration.CatalogClientConfig;
import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "catalog",
        url = "${feign.catalog.url}",
        fallback = CatalogClientFallback.class,
        configuration = CatalogClientConfig.class
)
public interface CatalogClient {

    @PostMapping("/catalog/goods/getGoodsList")
    GetGoodsListResponse getGoodsList(@RequestBody GetGoodsListRequest request);
}
