package com.dbp.proyectobackendmarketexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProyectoBackendMarketexchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoBackendMarketexchangeApplication.class, args);
	}

}
