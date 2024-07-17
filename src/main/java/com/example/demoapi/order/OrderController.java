package com.example.demoapi.order;

import jakarta.websocket.server.PathParam;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.example.demoapi.DemoapiApplication.API_VERSION;

@RestController
@RequestMapping(path = API_VERSION + "/orders")
class OrderController {
    private final OrderService orderService;

    @Autowired
    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    CollectionModel<EntityModel<Order>> getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/{id}")
    EntityModel<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    Order postOrder(@RequestBody Order order) {
        return orderService.saveOrder(order);
    }

    @PutMapping("/{id}")
    Order putOrder(@RequestBody Order order, @PathVariable Long id) {
        return orderService.updateOrder(order, id);
    }

    @DeleteMapping("/{id}")
    void deleteOrder(@PathVariable Long id) {
        orderService.removeOrder(id);
    }
}
