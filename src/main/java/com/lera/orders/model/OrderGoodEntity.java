package com.lera.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "orders_good")
@Getter
@Setter
@NoArgsConstructor
public class OrderGoodEntity {

    @EmbeddedId
    private OrderGoodId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "count", nullable = false, precision = 10, scale = 2)
    private BigDecimal count;

    @Column(name = "sum", nullable = false, precision = 10, scale = 2)
    private BigDecimal sum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    public OrderGoodEntity(OrderGoodId id, String name, BigDecimal price, BigDecimal count, BigDecimal sum) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.count = count;
        this.sum = sum;
    }

}
