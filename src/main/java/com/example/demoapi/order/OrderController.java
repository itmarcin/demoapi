package com.example.demoapi.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.demoapi.DemoapiApplication.API_VERSION;

@RestController
@RequestMapping(path = API_VERSION + "/orders")
class OrderController {
    private final OrderService orderService;
    private final OrderEntityModelAssembler orderEntityModelAssembler;

    @Autowired
    OrderController(OrderService orderService, OrderEntityModelAssembler orderEntityModelAssembler) {
        this.orderService = orderService;
        this.orderEntityModelAssembler = orderEntityModelAssembler;
    }

    @GetMapping()
    CollectionModel<EntityModel<Order>> getOrders() {
        return orderEntityModelAssembler.toCollectionModel(orderService.getOrders());
    }

    @GetMapping("/{id}")
    EntityModel<Order> getOrderById(@PathVariable Long id) {
        return orderEntityModelAssembler.toModel(orderService.getOrderById(id));
    }

    @PostMapping
    ResponseEntity<?> postOrder(@RequestBody Order order) {
        EntityModel<Order> entityModel = orderEntityModelAssembler.toModel(orderService.saveOrder(order));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> putOrder(@RequestBody Order order, @PathVariable Long id) {
        EntityModel<Order> entityModel = orderEntityModelAssembler.toModel(orderService.updateOrder(order, id));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.removeOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/cancel")
    ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        if (orderService.isOrderInProgress(id)) {
            return ResponseEntity.ok(orderEntityModelAssembler.toModel(orderService.cancelOrder(id)));
        }
        return orderEntityModelAssembler.getMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status");
    }

    @PutMapping("/{id}/complete")
    ResponseEntity<?> completeOrder(@PathVariable Long id) {
        if (orderService.isOrderInProgress(id)) {
            return ResponseEntity.ok(orderEntityModelAssembler.toModel(orderService.completeOrder(id)));
        }
        return orderEntityModelAssembler.getMethodNotAllowedResponse("You can complete only orders in the IN_PROGRESS status");
    }
}
