package com.arplanets.LiveSight.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PropertySource(value = "classpath:config/aws.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:config/database.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:config/order.properties", ignoreResourceNotFound = true)
public class LiveSightApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveSightApplication.class, args);
	}

}
