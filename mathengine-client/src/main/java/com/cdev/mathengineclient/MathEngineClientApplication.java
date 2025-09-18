package com.cdev.mathengineclient;

import com.cdev.mathengineclient.repository.UserAccountRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MathEngineClientApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MathEngineClientApplication.class, args);
        UserAccountRepository repository = context.getBean(UserAccountRepository.class);
    }

}
