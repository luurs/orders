package com.lera.orders.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        String userId,
        BigDecimal sum,
        List<GoodDto> goods
) {
    public record GoodDto(
            String name,
            BigDecimal price,
            BigDecimal count,
            BigDecimal sum,
            String externalId
    ) {
    }
}
