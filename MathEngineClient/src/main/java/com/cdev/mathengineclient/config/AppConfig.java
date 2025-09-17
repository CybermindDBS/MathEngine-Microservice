package com.cdev.mathengineclient.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class AppConfig {

    @Bean
    public ApplicationRunner checkDatabaseConnection(DatabaseClient client, ConnectionFactory connectionFactory) {
        return args -> {
            client.sql("SELECT 1")
                    .fetch()
                    .first()
                    .doOnError(err -> {
                        System.err.println("Database connection failed: " + err.getMessage());
                        // terminate the application
                        System.exit(1);
                    })
                    .block(); // block is okay here because it’s during startup
        };
    }

    @Bean
    @Profile({"default", "test"})
    @DependsOn("checkDatabaseConnection")
    public ApplicationRunner initDatabase(DatabaseClient databaseClient) {
        return args -> {
            databaseClient.sql("CREATE TABLE IF NOT EXISTS user_account (id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(255),password VARCHAR(255));")
                    .then()
                    .subscribe();
            databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS calculations (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id VARCHAR(255),
                            uuid VARCHAR(255),
                            main_expr TEXT,
                            variables TEXT,
                            functions TEXT,
                            result TEXT,
                            logs TEXT,
                            time_elapsed TEXT,
                            input_str TEXT,
                            output_str TEXT,
                            short_input_output TEXT,
                            date_time TIMESTAMP
                        );
                    """).then().subscribe();
            databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS user_functions (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id VARCHAR(100),
                            uuid VARCHAR(100),
                            functions CLOB
                        )
                    """).then().subscribe();

        };
    }

    @Profile("prod")
    @Bean
    @DependsOn("checkDatabaseConnection")
    public ApplicationRunner initMySQLDatabase(DatabaseClient databaseClient) {
        return args -> {
            databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS user_account (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255),
                password VARCHAR(255)
            );
        """).then().subscribe();

            databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS calculations (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(255),
                uuid VARCHAR(255),
                main_expr LONGTEXT,
                variables LONGTEXT,
                functions LONGTEXT,
                result LONGTEXT,
                logs LONGTEXT,
                time_elapsed TEXT,
                input_str LONGTEXT,
                output_str LONGTEXT,
                short_input_output LONGTEXT,
                date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """).then().subscribe();

            databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS user_functions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(100),
                uuid VARCHAR(100),
                functions LONGTEXT
            );
        """).then().subscribe();
        };
    }
}
