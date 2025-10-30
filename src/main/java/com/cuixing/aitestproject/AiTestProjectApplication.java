package com.cuixing.aitestproject;

import com.cuixing.aitestproject.chat.ChatWebSocketHandler;
import com.cuixing.aitestproject.chat.OllamaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AiTestProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTestProjectApplication.class, args);
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler(OllamaService ollamaService) {
        return new ChatWebSocketHandler(ollamaService);
    }
}
