package com.b1a4.cafeOn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CafeOnApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeOnApplication.class, args);
	}

}