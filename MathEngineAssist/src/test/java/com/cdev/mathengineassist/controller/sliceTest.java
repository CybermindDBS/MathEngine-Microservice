package com.cdev.mathengineassist.controller;

import com.cdev.mathengineassist.dto.RedisMapDTO;
import com.cdev.mathengineassist.service.MathEngineAssistService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.profiles.active=test", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class sliceTest {
    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    MathEngineAssistService mathEngineAssistService;

    RedisMapDTO value = new RedisMapDTO(URLEncoder.encode("9!*(10/2)+9"), "1814409");

    @Test
    public void saveTest() {
        Mockito.when(mathEngineAssistService.saveValue(value.getInput(), value.getOutput())).thenReturn(Mono.just(true));

        webTestClient.post().uri("/save").bodyValue(value).exchange().expectStatus().isOk().expectBody(String.class).value(val -> assertEquals("true", val));
    }

    @Test
    public void fetchTest() {
        Mockito.when(mathEngineAssistService.fetchValue(value.getInput())).thenReturn(Mono.just(value.getOutput()));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/fetch")
                        .queryParam("input", value.getInput())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(val -> assertEquals(value.getOutput(), val));
    }
}
