package com.example.demoapi.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class OrderService {
    //private final OrderRepository orderRepository;

    @Autowired
    OrderService() {
//        this.orderRepository = orderRepository;
    }
}
