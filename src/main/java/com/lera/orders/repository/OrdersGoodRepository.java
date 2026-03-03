package com.lera.orders.repository;

import com.lera.orders.model.OrdersGood;
import com.lera.orders.model.OrdersGoodId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersGoodRepository extends JpaRepository<OrdersGood, OrdersGoodId> {
}
