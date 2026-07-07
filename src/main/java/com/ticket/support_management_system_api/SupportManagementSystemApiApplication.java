package com.ticket.support_management_system_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SupportManagementSystemApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportManagementSystemApiApplication.class, args);
	}

}
