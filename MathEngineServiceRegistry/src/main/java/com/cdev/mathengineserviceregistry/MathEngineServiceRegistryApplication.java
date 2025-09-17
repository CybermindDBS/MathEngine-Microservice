package com.cdev.mathengineserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MathEngineServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(MathEngineServiceRegistryApplication.class, args);
	}

}
