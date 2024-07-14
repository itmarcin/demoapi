package com.example.demoapi.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    List<Order> getOrders() {
        return orderRepository.findAll();
    }

    Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    Order updateOrder(Order newOrder, Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setDescription(newOrder.getDescription());
                    order.setStatus(newOrder.getStatus());
                    return orderRepository.save(order);
                }).orElseGet(() -> {
                    return orderRepository.save(newOrder);
                });
    }

    public void removeOrder(Long id) {
        orderRepository.deleteById(id);
    }
}

