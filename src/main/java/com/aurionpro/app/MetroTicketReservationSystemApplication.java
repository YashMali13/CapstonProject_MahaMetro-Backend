package com.aurionpro.app;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableScheduling
public class MetroTicketReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetroTicketReservationSystemApplication.class, args);
	}

}
