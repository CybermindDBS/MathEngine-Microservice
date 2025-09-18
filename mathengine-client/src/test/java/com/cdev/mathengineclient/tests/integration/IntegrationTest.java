package com.cdev.mathengineclient.tests.integration;

import com.cdev.mathengineclient.pages.MathEnginePage;
import com.cdev.mathengineclient.service.CalculationService;
import com.cdev.mathengineclient.service.MathEngineClient;
import dto.Output;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class IntegrationTest {

    @LocalServerPort
    int port;
    @MockitoBean
    MathEngineClient mathEngineClient;

    String tc1 = "5+9!*(10/2)";

    @Test
    public void calcTest(@Autowired  WebDriver webDriver) {
        Output output = new Output();
        output.setMainExpr(tc1);
        output.setResult("1814405");
        Mockito.when(mathEngineClient.evaluate(any(String.class))).thenReturn(Mono.just(output));

        MathEnginePage page = new MathEnginePage(webDriver);
        webDriver.get("http://localhost:" + port);

        page.calculate(tc1);
        String result = page.getResult();
        assertEquals("1814405", result);
    }
}
