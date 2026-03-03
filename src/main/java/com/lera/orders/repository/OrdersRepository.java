package com.lera.orders.repository;

import com.lera.orders.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
