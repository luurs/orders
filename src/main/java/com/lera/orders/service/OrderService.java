package com.lera.orders.service;

import com.lera.orders.dto.ConfirmPaymentRequest;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.model.OrderEntity;
import com.lera.orders.model.OrderGoodEntity;
import com.lera.orders.model.OrderGoodId;
import com.lera.orders.repository.OrderRepository;
import com.lera.orders.util.ApiException;
import com.lera.orders.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogCacheService catalogCacheService;
    private final OrderValidator orderValidator;

    @Transactional
    public Long createOrder(String userId, BigDecimal sum, List<CreateOrderRequest.GoodDto> goods) {
        var orderRequest = new CreateOrderRequest(userId, sum, goods);
        List<String> externalIds = goods.stream()
                .map(CreateOrderRequest.GoodDto::externalId)
                .toList();
        var goodsList = catalogCacheService.getGoods(externalIds);

        orderValidator.validateOrder(orderRequest, goodsList);  // валидация, возможны исключения

        OrderEntity order = new OrderEntity(userId, sum, null, OrderStatus.NEW);

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

    @Transactional
    public void confirmPayment(Long orderId, Long paymentId, BigDecimal sum) {
        var orderRequest = new ConfirmPaymentRequest(orderId, paymentId, sum);
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Заказ не найден, orderId: " + orderId, HttpStatus.NOT_FOUND));

        orderValidator.validatePayment(order, orderRequest);

        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(paymentId);
    }
}
