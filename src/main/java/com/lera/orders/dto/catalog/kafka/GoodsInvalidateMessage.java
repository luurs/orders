package com.lera.orders.dto.catalog.kafka;

import java.util.List;

public record GoodsInvalidateMessage(
        List<GoodsItem> goods
) {
}
