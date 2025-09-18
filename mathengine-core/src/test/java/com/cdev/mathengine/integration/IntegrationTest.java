package com.cdev.mathengine.integration;

import com.cdev.mathengine.api.dto.Output;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.profiles.active=test", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class IntegrationTest {
    @Autowired
    WebTestClient webTestClient;

    String tc1 = "9!*(10/2)+9";
    String tc2 = "9! * (calc(10) / 2) + pi +  var\n" +
            "var = 10 / 5 * 2\n" +
            "\n" +
            "public int calc(int a) {\n" +
            "    logger.log(\"variable: \" + a);\n" +
            "    return a * 1000;\n" +
            "}";
    String tc3 = "9000!";


    String tc3Out;

    {
        try {
            tc3Out = Files.readString(
                    Paths.get(getClass().getClassLoader().getResource("tc3Out.txt").toURI()),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void evalTest() {
        webTestClient.post().uri("/api/evaluate").bodyValue(tc1).exchange().expectStatus().isOk().expectBody(Output.class).value(out -> assertEquals("1814409", out.getResult()));
    }

    @Test
    public void evalTest2() {
        webTestClient.post().uri("/api/evaluate").bodyValue(tc2).exchange().expectStatus().isOk().expectBody(Output.class).value(out -> {
            assertEquals("1814400007.141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117068", out.getResult());
            assertEquals("variable: 10\n", out.getLogs());
        });
    }

    @Test
    public void evalTest3() {
        Output out = webTestClient.post().uri("/api/evaluate").bodyValue(tc3).exchange().expectStatus().isOk().returnResult(Output.class).getResponseBody().blockFirst(Duration.ofSeconds(30));
        assertEquals(tc3Out, out.getResult());
    }
}
