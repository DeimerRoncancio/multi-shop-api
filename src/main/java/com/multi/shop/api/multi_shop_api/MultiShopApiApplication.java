package com.multi.shop.api.multi_shop_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MultiShopApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiShopApiApplication.class, args);
	}
}
