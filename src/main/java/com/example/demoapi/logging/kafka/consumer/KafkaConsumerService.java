package com.example.demoapi.logging.kafka.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LogManager.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "logs")
    public void listen(String message) {
        log.info("[Kafka_Consumer] Received message: {}", message);
    }
}
