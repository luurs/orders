package com.lera.orders.dto.good;

import java.math.BigDecimal;

public record CreateGoodRequest(
        String name,
        String description,
        BigDecimal price,
        String externalId
) {
}
