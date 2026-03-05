package com.lera.orders.repository;

import com.lera.orders.model.OrderGoodEntity;
import com.lera.orders.model.OrderGoodId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderGoodRepository extends JpaRepository<OrderGoodEntity, OrderGoodId> {
}
