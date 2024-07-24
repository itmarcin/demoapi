package com.example.demoapi.order;

import com.example.demoapi.logging.kafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


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

    EntityModel<Order> getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        kafkaProducerService.sendLog(String.format("Searching for the order: %d", order.getId()));
        return orderEntityModelAssembler.toModel(order);
    }

    CollectionModel<EntityModel<Order>> getOrders() {
        List<Order> orders = orderRepository.findAll();
        kafkaProducerService.sendLog("Searching for all orders");
        return orderEntityModelAssembler.toCollectionModel(orders);
    }

    ResponseEntity<?> saveOrder(Order order) {
        EntityModel<Order> entityModel = orderEntityModelAssembler.toModel(orderRepository.save(order));
        kafkaProducerService.sendLog(String.format("Saving the new user: %s", Objects.requireNonNull(entityModel.getContent())));
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

    ResponseEntity<?> removeOrder(Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    ResponseEntity<?> cancelOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() == Status.IN_PROGRESS) {
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(orderEntityModelAssembler.toModel(orderRepository.save(order)));
        }
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can't cancel an order that is in the " + order.getStatus() + " status"));
    }

    public ResponseEntity<?> completeOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() == Status.IN_PROGRESS) {
            order.setStatus(Status.COMPLETED);
            return ResponseEntity.ok(orderEntityModelAssembler.toModel(orderRepository.save(order)));
        }
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can't complete an order that is in the " + order.getStatus() + " status"));
    }
}

