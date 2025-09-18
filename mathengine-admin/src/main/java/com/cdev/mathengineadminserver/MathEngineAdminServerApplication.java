package com.cdev.mathengineadminserver;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class MathEngineAdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MathEngineAdminServerApplication.class, args);
    }

}
