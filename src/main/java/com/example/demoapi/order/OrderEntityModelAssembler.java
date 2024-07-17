package com.example.demoapi.order;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEntityModelAssembler implements RepresentationModelAssembler<Order, EntityModel<Order>> {

    @Override
    public EntityModel<Order> toModel(Order order) {

        return EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"));
    }

    @Override
    public CollectionModel<EntityModel<Order>> toCollectionModel(Iterable<? extends Order> orders) {
        List<EntityModel<Order>> modelOrders = ((List<Order>) orders).stream().map(this::toModel).toList();
        return CollectionModel.of(modelOrders, linkTo(methodOn(OrderController.class).getOrders()).withSelfRel());
    }
}
