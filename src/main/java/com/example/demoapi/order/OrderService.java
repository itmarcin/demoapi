package com.example.demoapi.order;

import com.example.demoapi.logging.kafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEntityModelAssembler orderEntityModelAssembler;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    OrderService(OrderRepository orderRepository, OrderEntityModelAssembler orderEntityModelAssembler, KafkaProducerService kafkaProducerService) {
        this.orderRepository = orderRepository;
        this.orderEntityModelAssembler = orderEntityModelAssembler;
        this.kafkaProducerService = kafkaProducerService;
    }

    Order getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        kafkaProducerService.sendLog(String.format("Searching for the order: %d", order.getId()));
        return order;
    }

    Collection<Order> getOrders() {
        List<Order> orders = orderRepository.findAll();
        kafkaProducerService.sendLog("Searching for all orders");
        return orders;
    }

    Order saveOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        kafkaProducerService.sendLog(String.format("Saving the new user: %s", savedOrder.toString()));
        return savedOrder;
    }

    Order updateOrder(Order newOrder, Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setDescription(newOrder.getDescription());
                    order.setStatus(newOrder.getStatus());
                    return orderRepository.save(order);
                }).orElseGet(() -> orderRepository.save(newOrder));
    }

    void removeOrder(Long id) {
        orderRepository.deleteById(id);
    }

    Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(Status.CANCELED);
        return orderRepository.save(order);
    }

    Order completeOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(Status.COMPLETED);
        return orderRepository.save(order);
    }

    boolean isOrderInProgress(Long id) {
        return this.getOrderById(id).isInProgress();
    }
}

