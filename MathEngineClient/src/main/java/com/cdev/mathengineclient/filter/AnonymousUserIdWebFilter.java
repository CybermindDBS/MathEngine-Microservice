package com.cdev.mathengineclient.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
public class AnonymousUserIdWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();

        if (!request.getCookies().containsKey("UUID")) {
            response.addCookie(
                    ResponseCookie.from("UUID", UUID.randomUUID().toString())
                            .path("/")
                            .maxAge(Duration.ofDays(365L * 20L))
                            .httpOnly(true)
                            .build()
            );
            response.setStatusCode(HttpStatus.FOUND);
            response.getHeaders().setLocation(URI.create("/"));
            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
