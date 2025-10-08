package com.arplanets.corexrapi.livesight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableAsync
@ServletComponentScan
public class LiveSightApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveSightApplication.class, args);
	}

}
