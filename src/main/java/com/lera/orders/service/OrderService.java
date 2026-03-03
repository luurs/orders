package com.lera.orders.service;

import com.lera.orders.clients.GoodClient;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.dto.good.GetGoodsListRequest;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.model.Orders;
import com.lera.orders.model.OrdersGood;
import com.lera.orders.model.OrdersGoodId;
import com.lera.orders.repository.OrdersGoodRepository;
import com.lera.orders.repository.OrdersRepository;
import com.lera.orders.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrdersGoodRepository ordersGoodRepository;
    private final GoodClient goodClient;
    private final OrderValidator orderValidator;

    @Transactional
    public Long createOrder(String userId, BigDecimal sum, List<CreateOrderRequest.GoodDto> goods) {
        var orderRequest = new CreateOrderRequest(userId, sum, goods);
        List<String> listExternalIds = goods.stream()
                .map(CreateOrderRequest.GoodDto::externalId)
                .toList();
        var goodsList = goodClient.getGoodsList(new GetGoodsListRequest(listExternalIds));

        orderValidator.validateOrder(orderRequest, goodsList);  // валидация, возможны исключения

        Orders order = new Orders(userId, sum, OrderStatus.NEW);
        order.setGoods(new ArrayList<>());

        goods.forEach(dto -> {

            OrdersGoodId id = new OrdersGoodId();
            id.setExternalId(dto.externalId());
            OrdersGood good = new OrdersGood(
                    dto.name(),
                    dto.price(),
                    dto.count(),
                    dto.sum()
            );
            good.setId(id);
            order.addGood(good);
        });

        ordersRepository.save(order);

        return order.getOrderId();
    }

}
