package de.mosesonline.mcptest.mcptest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class McpTestApplicationTests {

    @Test
    void contextLoads() {
    }


    public static void main(String[] args) {
        SpringApplication.from(McpTestApplication::main)
                .withAdditionalProfiles("local")
                .run(args);
    }

}
