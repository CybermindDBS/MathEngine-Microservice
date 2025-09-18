package com.cdev.mathengineclient.service;

import com.cdev.mathengineclient.entity.UserAccount;
import com.cdev.mathengineclient.repository.UserAccountRepository;
import dto.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class MathEngineClient {
    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final WebClient webClient;

    public MathEngineClient(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://MathEngineApiGateway/api").build();
    }

    public Mono<Output> evaluate(String input) {
        return webClient.post()
                .uri("/evaluate")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Output.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("API returned error: " + ex.getStatusCode());
                    System.err.println("API error body: " + ex.getResponseBodyAsString());

                    Output output = new Output();
                    output.setMainExpr(input.lines().findFirst().get());
                    String raw = ex.getResponseBodyAsString();
                    String errorMsg = raw.replaceAll("^\\{\"error\":\"", "")
                            .replaceAll("\"\\}$", "");
                    String statusCode = ex.getStatusCode().toString();
                    String pattern = "(?i).*Unlimited.*MathContext.*not.*supported.*";
                    String pattern2 = "(?i).*Non.*terminating.*";
                    String pattern3 = "(?i).*Divide.*by.*zero.*";
                    String pattern4 = "(?i).*Empty.*input.*";
                    String pattern5 = "(?i).*Bracket.*not.*closed.*properly.*";
                    String pattern6 = "(?i).*Variable.*not.*defined.*";
                    String pattern7 = "(?i).*Not.*a.*valid.*expression.*";
                    //org.codehaus.commons.compiler.CompileException
                    String pattern8 = "(?i).*commons.*compiler.*CompileException.*";
                    String pattern9 = "(?i).*DynamicCompileTimeException.*";
                    String pattern10 = "(?i).*NoSuchMethodException.*";
                    String pattern11 = "(?i).*429 TOO_MANY_REQUESTS.*";


                    if (errorMsg.matches(pattern))
                        errorMsg = "Unlimited MathContext is not supported.\nTip: use precision variable, e.g., precision = 100.";
                    else if (errorMsg.matches(pattern2))
                        errorMsg = "Non-terminating decimal expansion: no exact representation possible.\nTip: use precision variable, e.g., precision = 100.";
                    else if (errorMsg.matches(pattern3))
                        errorMsg = "Division by zero is not allowed.";
                    else if (errorMsg.matches(pattern4))
                        errorMsg = "Input is empty.";
                    else if (errorMsg.matches(pattern5))
                        errorMsg = "Mismatched or unclosed brackets.";
                    else if (errorMsg.matches(pattern6))
                        errorMsg = "Undefined variable.";
                    else if (errorMsg.matches(pattern7))
                        errorMsg = "Invalid expression.";
                    else if (errorMsg.matches(pattern8) || errorMsg.matches(pattern9))
                        errorMsg = "Compilation error.\n" + errorMsg;
                    else if (errorMsg.matches(pattern10))
                        errorMsg = "No such method found.\n" + errorMsg;
                    else if (statusCode.matches(pattern11))
                        errorMsg = statusCode;

                    output.setResult("Error. \n" + errorMsg);
                    return Mono.just(output);
                })
                .onErrorResume(Exception.class, ex -> {
                    System.err.println("Client error: " + ex.getMessage());
                    Output output = new Output();
                    output.setResult("Error. \n" + "Client error: " + ex.getMessage());
                    return Mono.just(output);
                });
    }

    public Mono<UserAccount> registerNewUser(String username, String password, String confirmPassword) {

        if (!password.equals(confirmPassword))
            return Mono.error(new IllegalArgumentException("Passwords do not match."));
        else
            return userAccountRepository.findUserAccountByUsername(username)
                    .flatMap((userAccount) -> Mono.<UserAccount>error(new IllegalArgumentException("Account already exists.")))
                    .switchIfEmpty(Mono.defer(() -> userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password)))));
    }
}
