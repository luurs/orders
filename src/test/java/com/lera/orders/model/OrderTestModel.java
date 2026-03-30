package com.lera.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderTestModel {

    @Column(name = "order_id")
    private long orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "total_sum", nullable = false)
    private BigDecimal totalSum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "payment_id")
    private Long paymentId;
}
