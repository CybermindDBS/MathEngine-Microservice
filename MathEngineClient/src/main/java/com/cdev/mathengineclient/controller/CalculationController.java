package com.cdev.mathengineclient.controller;

import com.cdev.mathengineclient.entity.Calculation;
import com.cdev.mathengineclient.entity.UserFunctions;
import com.cdev.mathengineclient.model.UserState;
import com.cdev.mathengineclient.service.CalculationService;
import com.cdev.mathengineclient.service.MathEngineClient;
import dto.CalculationIdDTO;
import dto.FunctionsDTO;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class CalculationController {
    @Autowired
    CalculationService calculationService;

    @Autowired
    MathEngineClient mathEngineClient;

    private static final Logger log = LoggerFactory.getLogger(CalculationController.class);

    @PostMapping("/new-calculation")
    Mono<String> newCalculation(ServerHttpRequest request, Model model, ServerWebExchange webExchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                .flatMap(auth -> {
                            if (auth.getPrincipal() instanceof OAuth2User oAuth2User)
                                return Mono.defer(() -> calculationService.createNewCalculation(oAuth2User.getAttributes().get("email").toString(), null));
                            else
                                return Mono.defer(() -> calculationService.createNewCalculation(auth.getName(), null));
                        }
                )
                .flatMap(calculation -> {
                    model.addAttribute("selectedCalculation", calculation);
                    return webExchange.getSession().map(webSession -> {
                        webSession.getAttributes().put("selectedCalculationId", calculation.getId());
                        return calculation;
                    });
                })
                .switchIfEmpty(Mono.defer(() -> calculationService.createNewCalculation(null, request.getCookies().get("UUID").getFirst().getValue())))
                .flatMap(calculation -> {
                    model.addAttribute("selectedCalculation", calculation);
                    return webExchange.getSession().map(webSession -> {
                        webSession.getAttributes().put("selectedCalculationId", calculation.getId());
                        return calculation;
                    });
                })
                .then(Mono.just("redirect:/"));
    }

    @PostMapping("/select-calculation")
    Mono<String> selectCalculation(@ModelAttribute CalculationIdDTO calculationIdDTO, ServerWebExchange webExchange) {
        return webExchange.getSession().map(session -> {
            session.getAttributes().put("selectedCalculationId", calculationIdDTO.getCalculationId());
            return session;
        }).thenReturn("redirect:/");
    }

    @PostMapping("/calculate")
    Mono<String> calculate(@ModelAttribute Calculation calculation, ServerWebExchange webExchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User)
                            return new UserState(oAuth2User.getAttributes().get("email").toString(), null, true);
                        else
                            return new UserState(authentication.getName(), null, false);
                    } else
                        return new UserState(null, webExchange.getRequest().getCookies().get("UUID").getFirst().getValue(), false);
                })
                .switchIfEmpty(Mono.just(new UserState(null, webExchange.getRequest().getCookies().get("UUID").getFirst().getValue(), false)))
                .flatMap(userState ->
                        webExchange.getSession()
                                .flatMap(webSession -> {
                                    Long selectedCalculationId = webSession.getAttribute("selectedCalculationId");
                                    calculation.setId(selectedCalculationId);
                                    if (userState.getUsername() != null)
                                        calculation.setUserId(userState.getUsername());
                                    else
                                        calculation.setUuid(userState.getUUID());

                                    return calculationService.getFunctions(userState.getUsername(), userState.getUUID())
                                            .map(UserFunctions::getFunctions)
                                            .switchIfEmpty(Mono.just(""))
                                            .flatMap(functionsStr ->
                                                    mathEngineClient.evaluate(calculation.getInput() + "\n" + functionsStr)
                                                            .flatMap(evaluation -> {
                                                                calculation.setMainExpr(evaluation.getMainExpr());
                                                                calculation.setResult(evaluation.getResult());
                                                                calculation.setFunctions(evaluation.getFunctions() != null ? evaluation.getFunctions().stream().reduce((accum, str) -> accum + "\n" + str).orElse("") : "");
                                                                calculation.setLogs(evaluation.getLogs());
                                                                calculation.setTimeElapsed(evaluation.getTimeElapsed());
                                                                String output = "";
                                                                output += calculation.getResult();
                                                                if (calculation.getLogs() != null)
                                                                    output += "\nLogs: \n" + calculation.getLogs();
                                                                if (calculation.getTimeElapsed() != null)
                                                                    output += "\nCalculated in: \n" + calculation.getTimeElapsed();
                                                                calculation.setOutput(output);
                                                                System.out.println(calculation);
                                                                calculation.setShortInputOutput((calculation.getMainExpr() != null ? calculation.getMainExpr() : (calculation.getInput().isEmpty() ? " " : calculation.getInput().split("\n")[0])) + "\n" + (!calculation.getResult().contains("Error") ? calculation.getResult() : "Error."));
                                                                calculation.setDateTime();
                                                                return calculationService.updateCalculation(calculation);
                                                            })
                                                            .doOnNext((calculation1)-> log.info("Calculation completed, result: " + calculation1.getResult() + ", userId: " + (userState.getUsername() == null ? userState.getUUID() : userState.getUsername()),
                                                                    //Logs as Json format in log file appender with json encoder.
                                                                    StructuredArguments.keyValue("userId", (userState.getUsername() == null ? userState.getUUID() : userState.getUsername())),
                                                                    StructuredArguments.keyValue("calculatedResult", calculation.getResult())))
                                            );
                                })
                )
                .thenReturn("redirect:/");

    }

    @PostMapping("/save-functions")
    Mono<String> saveFunctions(ServerHttpRequest request, @ModelAttribute FunctionsDTO function) {

        return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
                .filter(auth -> auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                .flatMap(auth -> calculationService.saveFunctions(function.getFunctions(), auth.getPrincipal() instanceof OAuth2User oAuth2User ? (String) oAuth2User.getAttributes().get("email") : auth.getName(), null).thenReturn("redirect:/"))
                .switchIfEmpty(calculationService.saveFunctions(function.getFunctions(), null, request.getCookies().get("UUID").getFirst().getValue()).thenReturn("redirect:/"));
    }
}
