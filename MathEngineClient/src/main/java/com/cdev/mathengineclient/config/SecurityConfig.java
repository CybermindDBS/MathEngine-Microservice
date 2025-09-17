package com.cdev.mathengineclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.server.WebSession;

import java.net.URI;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity security) {
        security.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.pathMatchers("/protected").authenticated().anyExchange().permitAll())
                .csrf(Customizer.withDefaults())
                .formLogin(formLoginSpec -> formLoginSpec
                        .loginPage("/login")
                        .authenticationFailureHandler((webFilterExchange, exception) -> webFilterExchange.getExchange().getSession()
                                .doOnNext(session -> session.getAttributes().put("loginError", exception.getMessage()))
                                .then(new RedirectServerAuthenticationFailureHandler("/login?error").onAuthenticationFailure(webFilterExchange, exception))
                        )
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logoutSpec -> logoutSpec
                        .logoutUrl("/logout")
                        .requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout", HttpMethod.POST))
                        .logoutSuccessHandler((exchange, authentication) -> {
                            exchange.getExchange().getResponse()
                                    .setStatusCode(HttpStatus.FOUND); // 302 redirect
                            exchange.getExchange().getResponse()
                                    .getHeaders().setLocation(URI.create("/"));
                            return exchange.getExchange().getSession().flatMap(WebSession::invalidate)
                                    .then(exchange.getExchange().getResponse().setComplete());
                        })
                );

        return security.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
