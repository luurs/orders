package com.lera.orders.validator;

import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.dto.good.GetGoodsListResponse;
import com.lera.orders.util.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderValidator {

    // todo прохождение валидации json'а с таблицами БД
    // todo если какая-то из валидаций не проходит, выбросить ошибку валидации - сделать ValidationException

    public void validateOrder(CreateOrderRequest orderRequest, GetGoodsListResponse goodsList) {
        validatePrice(orderRequest, goodsList);
        validateSum(orderRequest);
        validateFinalSum(orderRequest);
    }

    private void validatePrice(CreateOrderRequest orderRequest, GetGoodsListResponse goodsList) {

        Map<String, GetGoodsListResponse.GoodDto> goodsMap =
                goodsList.goods().stream()
                        .collect(Collectors.toMap(
                                GetGoodsListResponse.GoodDto::externalId,
                                Function.identity()
                        ));

        for (CreateOrderRequest.GoodDto good : orderRequest.goods()) {
            var requestId = good.externalId();
            GetGoodsListResponse.GoodDto found = goodsMap.get(requestId);

            if (found == null) {
                throw new RuntimeException("Good not found: " + requestId);
            }

            if (!good.price().equals(found.price())) {
                throw new ValidationException("Цена товара в запросе не совпадает с ценой товара в сервисе catalog", HttpStatus.UNPROCESSABLE_ENTITY);
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
                    throw new ValidationException("Сумма товара не соответствует sum, externalId: " + good.externalId(), HttpStatus.UNPROCESSABLE_ENTITY);
                });

    }

    private void validateFinalSum(CreateOrderRequest orderRequest) {

        BigDecimal calculatedSum = orderRequest.goods().stream()
                .map(CreateOrderRequest.GoodDto::sum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedSum.compareTo(orderRequest.sum()) != 0) {
            throw new ValidationException("Сумма заказа не равна сумме товаров", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
