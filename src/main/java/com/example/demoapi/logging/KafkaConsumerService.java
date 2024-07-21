package com.example.demoapi.logging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "logs")
    public void listen(String message) {
        System.out.println("Recieved message: " + message);
    }
}
