package com.lera.orders.controller;

import com.lera.orders.dto.ConfirmPaymentRequest;
import com.lera.orders.dto.CreateOrderResponse;
import com.lera.orders.dto.CreateOrderRequest;
import com.lera.orders.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
        var orderId = orderService.createOrder(request.userId(), request.sum(), request.goods());
        return new CreateOrderResponse(orderId);
    }

    @PostMapping("/confirmPayment")
    public void confirmPayment(@RequestBody ConfirmPaymentRequest request) {
        orderService.confirmPayment(request.orderId(), request.paymentId(), request.sum());
    }
}
