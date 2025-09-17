package com.cdev.mathengine.monitor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MathEngineAssistMonitor {

    private final WebClient webClient;
    private final AtomicBoolean assistEnabled = new AtomicBoolean(false);

    public MathEngineAssistMonitor(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://MathEngineAssist").build();
    }

    @Bean
    public CommandLineRunner monitorMathEngineAssist() {
        return args -> {
            Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(this::checkHealth, 0, 10, TimeUnit.SECONDS);
        };
    }

    private void checkHealth() {
        try {
            webClient.get().uri("/actuator/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            assistEnabled.set(true);
        } catch (Exception e) {
            assistEnabled.set(false);
            System.out.println("MathEngineAssist is offline");
        }
    }

    public boolean isMathEngineAssistOnline() {
        return assistEnabled.get();
    }
}
