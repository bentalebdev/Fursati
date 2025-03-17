package com.ismagi.Fursati;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("com.ismagi.Fursati.repository")

@SpringBootApplication
public class FursatiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FursatiApplication.class, args);
	}

}
