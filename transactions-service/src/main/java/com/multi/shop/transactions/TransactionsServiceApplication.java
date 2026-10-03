package com.multi.shop.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TransactionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsServiceApplication.class, args);
	}
}
