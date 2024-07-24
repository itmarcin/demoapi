package com.example.demoapi.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<?> postOrder(@RequestBody Order order) {
        return orderService.saveOrder(order);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> putOrder(@RequestBody Order order, @PathVariable Long id) {
        return orderService.updateOrder(order, id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        return orderService.removeOrder(id);
    }

    @DeleteMapping("/{id}/cancel")
    ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PutMapping("/{id}/complete")
    ResponseEntity<?> completeOrder(@PathVariable Long id) {
        return orderService.completeOrder(id);
    }
}
