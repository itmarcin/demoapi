package com.example.demoapi.order;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, Long> {
// Using default MongoRepository implementation. No need for overriding/implementing methods.
}
