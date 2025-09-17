package com.cdev.mathengineclient.controller;

import com.cdev.mathengineclient.model.UserState;
import com.cdev.mathengineclient.service.CalculationService;
import com.cdev.mathengineclient.service.MathEngineClient;
import dto.FunctionsDTO;
import dto.NewUserAccountDTO;
import dto.PaginationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
public class MathEngineWebController {
    @Autowired
    MathEngineClient service;
    @Autowired
    CalculationService calculationService;

    @GetMapping({"/", "/login", "/register"})
    public Mono<String> showHome(ServerHttpRequest request, Model model, ServerWebExchange webExchange) {

        return webExchange.getSession()
                .doOnNext(session -> {
                            String error = (String) session.getAttributes().get("registerError");
                            if (error != null) {
                                model.addAttribute("registerError", error);
                                session.getAttributes().remove("registerError");
                            }

                            error = (String) session.getAttributes().get("loginError");
                            if (error != null) {
                                model.addAttribute("loginError", error);
                                session.getAttributes().remove("loginError");
                            }

                            session.getAttributes().putIfAbsent("calcPage", 0);
                        }
                )
                .then(ReactiveSecurityContextHolder.getContext()
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
                )
                .flatMap(userState -> {

                    String username = userState.getUsername();
                    String UUID = userState.getUUID();
                    boolean isOAuth2User = userState.isOAuth2User();

                    model.addAttribute("userAuthenticated", username != null && !username.isEmpty());
                    return calculationService.getFunctions(username, UUID)
                            .map(userFunctions -> model.addAttribute("function", new FunctionsDTO(userFunctions.getFunctions())))
                            .switchIfEmpty(Mono.just(model.addAttribute("function", new FunctionsDTO())))
                            .then(webExchange.getSession()
                                    .flatMap(webSession -> {
                                        Long selectedCalculationId = (Long) webSession.getAttributes().get("selectedCalculationId");
                                        if (selectedCalculationId != null) {
                                            return calculationService.getCalculation(selectedCalculationId, username, UUID)
                                                    .map(calculation -> {
                                                        System.out.println("1. username: " + username + ", UUID: " + UUID + ", calc-id: " + selectedCalculationId);
                                                        model.addAttribute("selectedCalculation", calculation);
                                                        return calculation;
                                                    });
                                        } else {
                                            return calculationService.getLatestCalculation(username, UUID)
                                                    .map(calculation -> {
                                                        System.out.println("2. username: " + username + ", UUID: " + UUID + ", calc-id: " + selectedCalculationId);
                                                        model.addAttribute("selectedCalculation", calculation);
                                                        webSession.getAttributes().put("selectedCalculationId", calculation.getId());
                                                        return calculation;
                                                    })
                                                    .switchIfEmpty(Mono.defer(() ->
                                                            calculationService.createNewCalculation(username, UUID)
                                                                    .doOnNext(calculation -> {
                                                                        System.out.println("3. username: " + username + ", UUID: " + UUID + ", calc-id: " + selectedCalculationId);
                                                                        model.addAttribute("selectedCalculation", calculation);
                                                                        webSession.getAttributes().put("selectedCalculationId", calculation.getId());

                                                                    })
                                                    ));
                                        }
                                    })
                            )
                            .then(webExchange.getSession().flatMap(webSession -> calculationService.getCalculations(username, UUID, 10, ((Integer) webSession.getAttributes().get("calcPage")) * 10)
                                    .collectList()
                                    .doOnNext(calculations -> model.addAttribute("calculations", calculations))))
                            .then(webExchange.getSession()
                                    .flatMap(webSession ->
                                            calculationService.getTotalCalculationsCount(userState.getUsername(), userState.getUUID())
                                                    .doOnNext(totalCount -> {
                                                        webSession.getAttributes().putIfAbsent("calcPage", 0);
                                                        Integer currentPage = (Integer) webSession.getAttributes().get("calcPage");
                                                        long lastPage = (long) Math.ceil(((double) totalCount) / 10d) - 1;
                                                        if (lastPage == -1) lastPage = 0;
                                                        if (currentPage == 0) model.addAttribute("disablePrev", true);
                                                        else model.addAttribute("disablePrev", false);
                                                        if (currentPage == lastPage)
                                                            model.addAttribute("disableNext", true);
                                                        else model.addAttribute("disableNext", false);
                                                    })
                                    )
                            );
                })
                .then(webExchange.getSession().flatMap(webSession -> {
                    if (request.getURI().getPath().equals("/login")) {
                        webSession.getAttributes().remove("selectedCalculationId");
                        webSession.getAttributes().remove("calcPage");
                    }
                    return Mono.empty();
                }))
                .then(Mono.just("home"));
    }

    @PostMapping("/navigate-pagination")
    public Mono<String> navigatePagination(@ModelAttribute PaginationDTO paginationDTO, ServerWebExchange webExchange, Model model) {
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
                                .flatMap(webSession ->
                                        calculationService.getTotalCalculationsCount(userState.getUsername(), userState.getUUID())
                                                .doOnNext(totalCount -> {
                                                    webSession.getAttributes().putIfAbsent("calcPage", 0);
                                                    Long currentPage = (Long) webSession.getAttributes().get("calcPage");
                                                    long lastPage = (long) Math.ceil(((double) totalCount) / 10d) - 1;
                                                    if (lastPage == -1) lastPage = 0;
                                                    if (paginationDTO.action.equals("next")) {
                                                        if (currentPage < lastPage)
                                                            webSession.getAttributes().put("calcPage", currentPage + 1);
                                                    } else {
                                                        if (currentPage > 0)
                                                            webSession.getAttributes().put("calcPage", currentPage - 1);

                                                    }
                                                })
                                )
                )
                .thenReturn("redirect:/");
    }

    @PostMapping("/register")
    public Mono<String> registerUser(@ModelAttribute NewUserAccountDTO userAccount, ServerWebExchange webExchange) {
        return service.registerNewUser(userAccount.getUsername(), userAccount.getPassword(), userAccount.getConfirmPassword())
                .map(user -> "redirect:/login")
                .onErrorResume((exception) -> webExchange.getSession().doOnNext(session -> session.getAttributes().put("registerError", exception.getMessage())).thenReturn("redirect:/register?error"));
    }


    @GetMapping("/protected")
    public Mono<String> protectedPage(Model model, ServerHttpRequest request) {
        model.addAttribute("userAuthenticated", true);
        model.addAttribute("function", new FunctionsDTO());
        return Mono.just("home");
    }
}
