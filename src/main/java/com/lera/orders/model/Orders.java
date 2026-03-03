package com.lera.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "total_sum", nullable = false)
    private BigDecimal totalSum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersGood> goods;

    public Orders (String userId, BigDecimal totalSum, OrderStatus status) {
        this.userId = userId;
        this.totalSum = totalSum;
        this.status = status;
    }

    public void addGood(OrdersGood good) {
        goods.add(good);
        good.setOrder(this);
    }
}
