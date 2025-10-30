package com.b1a4.cafeOn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling	// 매일 새벽 3시에 최근일주일 조회수 업데이트하는 스케줄링 활성화
@SpringBootApplication
public class CafeOnApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeOnApplication.class, args);
	}

}