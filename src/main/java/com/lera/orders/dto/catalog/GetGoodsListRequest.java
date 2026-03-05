package com.lera.orders.dto.catalog;

import java.util.List;

public record GetGoodsListRequest(
        List<String> externalIds
) {
}
