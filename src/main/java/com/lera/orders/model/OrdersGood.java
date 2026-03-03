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
public class OrdersGood {

    @EmbeddedId
    private OrdersGoodId id;

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
    private Orders order;

    public OrdersGood(String name, BigDecimal price, BigDecimal count, BigDecimal sum) {
        this.name = name;
        this.price = price;
        this.count = count;
        this.sum = sum;
    }

}
