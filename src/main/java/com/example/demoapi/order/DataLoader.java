package com.example.demoapi.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
class DataLoader {

    private final static Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner initDatabase(OrderRepository orderRepository) {
        return args -> {
            orderRepository.deleteAll(); // Clean up any existing data
            logger.info("Preloading " + orderRepository.save(new Order(1L, Status.IN_PROGRESS, "Meat")));
            logger.info("Preloading " + orderRepository.save(new Order(2L, Status.CANCELED, "Beans")));
            logger.info("Preloading " + orderRepository.save(new Order(3L, Status.COMPLETED, "Tacos")));
            logger.info("Preloading " + orderRepository.save(new Order(4L, Status.IN_PROGRESS, "Meat")));
        };
    }
}