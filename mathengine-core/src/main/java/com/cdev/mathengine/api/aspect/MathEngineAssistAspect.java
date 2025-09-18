package com.cdev.mathengine.api.aspect;

import com.cdev.mathengine.api.core.ExpressionEvaluator;
import com.cdev.mathengine.api.dto.RedisMapDTO;
import com.cdev.mathengine.monitor.MathEngineAssistMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Aspect
@Component
public class MathEngineAssistAspect {

    @Value("${mathengineassist.config.min_wait_to_save}")
    int minWaitToSave;
    @Autowired
    WebClient webClient;
    @Autowired
    MathEngineAssistMonitor mathEngineAssistMonitor;

    @Around("@annotation(com.cdev.mathengine.api.annotation.MathAssistEnabled)")
    public Object method(ProceedingJoinPoint pjp) {
        System.out.println("Aspect Method Started");

        if (mathEngineAssistMonitor.isMathEngineAssistOnline()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<HashMap<String, Object>> future = executor.submit(() -> {
                HashMap<String, Object> result = null;
                try {
                    result = (HashMap<String, Object>) pjp.proceed(pjp.getArgs());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return result;
            });

            HashMap<String, Object> result;
            try {
                result = future.get(minWaitToSave, TimeUnit.MILLISECONDS);
                System.out.println("Fast Calculation.");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {

                String hashedInput = hashInput((String) pjp.getArgs()[0]);

                String output = webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/fetch")
                                .queryParam("input", hashedInput)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                System.out.println("Stored Value: " + output);
                boolean valuePresent = output != null && !(output.isBlank() || output.isEmpty());

                if (valuePresent) {
                    System.out.println("Value Present!");
                    result = new HashMap<>();

                    result.put("mainExpr", ((String) pjp.getArgs()[0]).split("\n")[0]);
                    result.put("variables", new ArrayList<>());
                    result.put("functions", new ArrayList<>());
                    result.put("result", output);
                    result.put("logs", "");
                    result.put("timeElapsed", 0 + "s");
                    return result;
                } else {
                    System.out.println("Value Not Present!");
                    try {
                        result = future.get();

                        String finalResult = result.get("logs") == null || ((String) result.get("logs")).isBlank() ? (String) result.get("result") : ((String) result.get("result")) + "\nLogs:\n" + result.get("logs");
                        RedisMapDTO redisMapDTO = new RedisMapDTO(hashedInput, finalResult);
                        Boolean saved = webClient.post()
                                .uri("/save")
                                .bodyValue(redisMapDTO)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .onErrorResume((error) -> {
                                    System.out.println(error.getMessage());
                                    return Mono.just(false);
                                })
                                .block();
                        if (saved != null && saved) System.out.println("Saved value!");
                        else System.out.println("Value still not saved!");

                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } finally {
                executor.shutdown();
            }
            return result;
        } else {
            try {
                return pjp.proceed(pjp.getArgs());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String hashInput(String input) {
        HashMap<String, Object> parsedMap = ExpressionEvaluator.parseVariablesAndClasses(input);
        StringBuilder hashInput = new StringBuilder();

        String mainExpr = input.split("\n")[0];
        HashMap<String, String> variables = (HashMap<String, String>) parsedMap.get("Variables");
        ArrayList<String> imports = (ArrayList<String>) parsedMap.get("Imports");
        HashMap<String, String> userDefinedFunctions = (HashMap<String, String>) parsedMap.get("UserDefinedFunctions");

        String newLine = "\n";
        hashInput.append(mainExpr.replace(" ", "")).append(newLine);
        variables.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).sorted().map(entryStr -> entryStr.replace(" ", "")).forEach((parsedStr) -> hashInput.append(parsedStr).append(newLine));
        imports.stream().sorted().filter(importLine -> !importLine.isBlank()).map(importLine -> importLine.replace(" ", "")).forEach(importLine -> hashInput.append(importLine).append(newLine));
        userDefinedFunctions.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getValue).filter(value -> !value.isBlank()).forEach((value) -> hashInput.append(value).append(newLine));

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = messageDigest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));
        String hashedStr = Base64.getUrlEncoder().encodeToString(bytes);

        return hashedStr;
    }
}
