package com.lera.orders.clients;

import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import com.lera.orders.util.ServiceUnavailableException;

public class CatalogClientFallback implements CatalogClient{

    @Override
    public GetGoodsListResponse getGoodsList(GetGoodsListRequest request) {
        throw new ServiceUnavailableException("Catalog service is unavailable");
    }
}
