package org.cytobank.springboot3demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Springboot3demoApplication {
	public static void main(String[] args) {
		SpringApplication.run(Springboot3demoApplication.class, args);
	}

}
