package com.lera.orders.clients;

import com.lera.orders.dto.good.CreateGoodRequest;
import com.lera.orders.dto.good.CreateGoodResponse;
import com.lera.orders.dto.good.GetGoodsListRequest;
import com.lera.orders.dto.good.GetGoodsListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalog", url = "${feign.catalog.url}")
public interface GoodClient {

    @PostMapping("/catalog/goods/createGood")
    CreateGoodResponse addGood(@RequestBody CreateGoodRequest request);

    @PostMapping("/catalog/goods/getGoodsList")
    GetGoodsListResponse getGoodsList(@RequestBody GetGoodsListRequest request);
}
