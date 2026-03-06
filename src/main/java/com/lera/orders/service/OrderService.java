package com.lera.orders.service;

import com.lera.orders.clients.CatalogClient;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.model.OrderEntity;
import com.lera.orders.model.OrderGoodEntity;
import com.lera.orders.model.OrderGoodId;
import com.lera.orders.repository.OrderGoodRepository;
import com.lera.orders.repository.OrderRepository;
import com.lera.orders.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderGoodRepository orderGoodRepository;
    private final CatalogClient catalogClient;
    private final OrderValidator orderValidator;

    @Transactional
    public Long createOrder(String userId, BigDecimal sum, List<CreateOrderRequest.GoodDto> goods) {
        var orderRequest = new CreateOrderRequest(userId, sum, goods);
        List<String> externalIds = goods.stream()
                .map(CreateOrderRequest.GoodDto::externalId)
                .toList();
        var goodsList = catalogClient.getGoodsList(new GetGoodsListRequest(externalIds));

        orderValidator.validateOrder(orderRequest, goodsList);  // валидация, возможны исключения

        OrderEntity order = new OrderEntity(userId, sum, OrderStatus.NEW);

        goods.forEach(dto -> {

            OrderGoodId id = new OrderGoodId();
            id.setExternalId(dto.externalId());
            OrderGoodEntity good = new OrderGoodEntity(
                    id,
                    dto.name(),
                    dto.price(),
                    dto.count(),
                    dto.sum()
            );
            order.addGood(good);
        });

        return orderRepository.save(order).getOrderId();
    }

}
