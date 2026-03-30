package com.lera.orders.validator;

import com.lera.orders.dto.ConfirmPaymentRequest;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;
import com.lera.orders.model.OrderEntity;
import com.lera.orders.model.OrderStatus;
import com.lera.orders.util.ApiException;
import com.lera.orders.util.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderValidator {

    public void validateOrder(CreateOrderRequest orderRequest, GetGoodsListResponse goodsList) {
        validatePrice(orderRequest, goodsList);
        validateSum(orderRequest);
        validateFinalSum(orderRequest);
    }

    public void validatePayment(OrderEntity order, ConfirmPaymentRequest orderRequest) {
        validateStatus(order);
        validateSum(order, orderRequest);
    }


    private void validateStatus(OrderEntity order) {
        if (!order.getStatus().equals(OrderStatus.NEW)) {
            throw new ValidationException("Invalid order status");
        }
    }

    private void validateSum(OrderEntity order, ConfirmPaymentRequest orderRequest) {
        if (!order.getTotalSum().equals(orderRequest.sum())) {
            throw new ValidationException("The amount in the request does not match the order amount in the database");
        }
    }

    private void validatePrice(CreateOrderRequest orderRequest, GetGoodsListResponse goodsList) {

        Map<String, GetGoodsListResponse.GoodDto> goodsMap =
                goodsList.goods().stream()
                        .collect(Collectors.toMap(
                                GetGoodsListResponse.GoodDto::externalId,
                                Function.identity()
                        ));

        for (CreateOrderRequest.GoodDto good : orderRequest.goods()) {
            var externalId = good.externalId();
            GetGoodsListResponse.GoodDto found = goodsMap.get(externalId);

            if (found == null) {
                throw new ApiException("Good not found: " + externalId, HttpStatus.NOT_FOUND);
            }

            if (!good.price().equals(found.price())) {
                throw new ValidationException("The price of the product in the request does not match the price of the product in the catalog service");
            }
        }

    }

    private void validateSum(CreateOrderRequest orderRequest) {

        orderRequest.goods().stream()
                .filter(good ->
                        good.price()
                                .multiply(good.count())
                                .compareTo(good.sum()) != 0
                )
                .findFirst()
                .ifPresent(good -> {
                    throw new ValidationException("The amount of the item does not match the sum, externalId: " + good.externalId());
                });

    }

    private void validateFinalSum(CreateOrderRequest orderRequest) {

        BigDecimal calculatedSum = orderRequest.goods().stream()
                .map(CreateOrderRequest.GoodDto::sum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedSum.compareTo(orderRequest.sum()) != 0) {
            throw new ValidationException("The order amount is not equal to the total amount of goods");
        }
    }
}
