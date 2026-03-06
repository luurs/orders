package com.lera.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderGoodId implements Serializable {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "external_id", length = 100)
    private String externalId;
}
