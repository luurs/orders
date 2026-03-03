package com.lera.orders.dto.good;

import java.util.List;

public record GetGoodsListRequest(
        List<String> externalIds
) {
}
