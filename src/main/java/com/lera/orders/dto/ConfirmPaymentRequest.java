package com.lera.orders.dto;

import java.math.BigDecimal;

public record ConfirmPaymentRequest(
        Long orderId,
        Long paymentId,
        BigDecimal sum
) {
}
