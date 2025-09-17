package com.cdev.mathengineclient.config;

import brave.propagation.TraceContext;
import com.cdev.mathengineclient.controller.CalculationController;
import io.micrometer.observation.ObservationRegistry;
import io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(CalculationController.class);

    @Bean
    @LoadBalanced
    public WebClient.Builder webClient(ObservationRegistry observationRegistry) {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(100 * 1024 * 1024) // 100 MB for large responses, note: JVM max heap size should support this.
                        )
                        .build());
    }
}



