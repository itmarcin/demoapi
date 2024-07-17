package com.example.demoapi.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEntityModelAssembler orderEntityModelAssembler;

    @Autowired
    OrderService(OrderRepository orderRepository, OrderEntityModelAssembler orderEntityModelAssembler) {
        this.orderRepository = orderRepository;
        this.orderEntityModelAssembler = orderEntityModelAssembler;
    }

     EntityModel<Order> getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return orderEntityModelAssembler.toModel(order);
    }

    CollectionModel<EntityModel<Order>> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orderEntityModelAssembler.toCollectionModel(orders);
    }

    ResponseEntity<?> saveOrder(Order order) {
        EntityModel<Order> entityModel = orderEntityModelAssembler.toModel(orderRepository.save(order));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    ResponseEntity<?> updateOrder(Order newOrder, Long id) {
        Order updatedOrder = orderRepository.findById(id)
                .map(order -> {
                    order.setDescription(newOrder.getDescription());
                    order.setStatus(newOrder.getStatus());
                    return orderRepository.save(order);
                }).orElseGet(() -> {
                    return orderRepository.save(newOrder);
                });

        EntityModel<Order> entityModel = orderEntityModelAssembler.toModel(updatedOrder);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    public void removeOrder(Long id) {
        orderRepository.deleteById(id);
    }
}

