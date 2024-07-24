package com.example.demoapi.logging.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "logs")
    public void listen(String message) {
        System.out.println("[Kafka_Consumer] Received message: " + message);
    }
}
