package com.cdev.mathengine.api.controller;

import com.cdev.mathengine.api.core.ExpressionEvaluator;
import com.cdev.mathengine.api.dto.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class MathEngineController {

    @Autowired
    ExpressionEvaluator expressionEvaluator;

    @PostMapping("/evaluate")
    public Mono<ResponseEntity<Output>> evaluate(@RequestBody(required = false) String input) {
        return Mono.fromCallable(() -> ResponseEntity.ok(new Output(expressionEvaluator.evaluate(input))))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(new Output(e.getMessage()))));
    }
}
