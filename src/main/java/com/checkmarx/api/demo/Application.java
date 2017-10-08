package com.checkmarx.api.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	CommandLineRunner lookup(CxPortalClient portal) {
		return args -> {

			final String session = portal.login("admin@cx", "Im@hom3y!!");
			portal.getAllUsers(session);
		};
	}
}
