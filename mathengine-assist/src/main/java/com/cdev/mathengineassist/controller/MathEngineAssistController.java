package com.cdev.mathengineassist.controller;

import com.cdev.mathengineassist.dto.RedisMapDTO;
import com.cdev.mathengineassist.service.MathEngineAssistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class MathEngineAssistController {
    @Autowired
    MathEngineAssistService service;


    @GetMapping(value = "/fetch", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> fetch(@RequestParam String input) {
        return service.fetchValue(input);
    }

    @PostMapping("/save")
    public Mono<Boolean> save(@RequestBody RedisMapDTO redisMapDTO) {
        System.out.println(redisMapDTO.getInput() + "     :     " + redisMapDTO.getOutput());
        return service.saveValue(redisMapDTO.getInput(), redisMapDTO.getOutput());
    }
}
