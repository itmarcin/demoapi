package com.example.demoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class DemoapiApplication {

	public static final String API_VERSION = "api/v1";

	public static void main(String[] args) {
		SpringApplication.run(DemoapiApplication.class, args);
	}

}
