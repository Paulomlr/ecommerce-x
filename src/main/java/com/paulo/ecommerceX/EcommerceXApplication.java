package com.paulo.ecommerceX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EcommerceXApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceXApplication.class, args);
	}

}
