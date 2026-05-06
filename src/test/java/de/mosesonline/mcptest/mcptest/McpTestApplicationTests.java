package de.mosesonline.mcptest.mcptest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.docker.compose.skip.in-tests=false",
        }
)
@ActiveProfiles("local")
class McpTestApplicationTests {

    public static void main(String[] args) {
        SpringApplication.from(McpTestApplication::main)
                .withAdditionalProfiles("local")
                .run(args);
    }

    @Test
    void contextLoads() {
    }

}
