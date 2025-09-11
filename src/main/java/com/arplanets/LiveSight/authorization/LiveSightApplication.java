package com.arplanets.LiveSight.authorization;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableScheduling
public class LiveSightApplication {

	public static void main(String[] args) {
		String hourlyDate = LocalDateTime.now(ZoneId.of("Asia/Taipei"))
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
		MDC.put("hourlyDate", "live-sight-logs-" + hourlyDate);
		SpringApplication.run(LiveSightApplication.class, args);
	}

}
