package com.cuixing.aitestproject;

import com.cuixing.aitestproject.chat.ChatWebSocketHandler;
import com.cuixing.aitestproject.chat.OllamaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class AiTestProjectApplication {

    public static void main(String[] args) {
        // 修复 Windows 终端中文乱码问题
        // 设置标准输出和错误输出使用 UTF-8 编码
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        
        // 设置控制台编码为 UTF-8（Windows 特殊处理）
        try {
            System.setProperty("file.encoding", "UTF-8");
            var field = java.nio.charset.Charset.class.getDeclaredField("defaultCharset");
            field.setAccessible(true);
            field.set(null, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果反射失败，至少确保 JVM 参数生效
        }
        
        SpringApplication.run(AiTestProjectApplication.class, args);
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler(OllamaService ollamaService) {
        return new ChatWebSocketHandler(ollamaService);
    }
}
