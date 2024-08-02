package com.example.demoapi.health;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.actuate.health.Health;

import java.util.Properties;

import static com.example.demoapi.DemoapiApplication.API_VERSION;

@RestController
@RequestMapping(path = API_VERSION + "/health")
public class HealthCheckController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @GetMapping("/liveness")
    public Health liveness() {
        return Health.up().build();
    }

    @GetMapping("/readiness")
    public Health readiness() {
        Health.Builder healthBuilder = Health.up();

        // Check MongoDB
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            healthBuilder.withDetail("MongoDB", "Available");
        } catch (Exception e) {
            healthBuilder.withDetail("MongoDB", "Not Available").down(e);
        }

        // Check Kafka
        Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        try (Admin adminClient = AdminClient.create(properties)) {
            adminClient.describeCluster()
                    .nodes()
                    .get();
            healthBuilder.withDetail("Kafka", "Available");
        } catch (Exception e) {
            healthBuilder.withDetail("Kafka", "Not Available").down(e);
        }

        return healthBuilder.build();
    }
}
