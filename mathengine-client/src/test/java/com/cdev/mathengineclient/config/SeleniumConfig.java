package com.cdev.mathengineclient.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {

    @Bean(destroyMethod = "quit")
    public WebDriver webDriver() {
        ChromeDriver driver = new ChromeDriver();
        return driver;
    }
}
