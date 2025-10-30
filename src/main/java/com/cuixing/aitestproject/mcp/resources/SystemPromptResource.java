package com.cuixing.aitestproject.mcp.resources;

import com.cuixing.aitestproject.mcp.core.McpResourceProvider;
import com.cuixing.aitestproject.mcp.model.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP 资源 - 系统提示词
 * 提供系统提示词模板访问
 */
@Component
public class SystemPromptResource implements McpResourceProvider {
    
    @Value("${mcp.prompt.path:react_prompt.txt}")
    private String promptPath;
    
    @Override
    public Object readResource(String uri) throws Exception {
        // URI 格式: prompt://system/react 或 prompt://system/custom
        String promptType = uri.replace("prompt://system/", "");
        
        return switch (promptType) {
            case "react" -> loadReactPrompt();
            case "assistant" -> getAssistantPrompt();
            default -> "未知的提示词类型: " + promptType;
        };
    }
    
    @Override
    public McpResource getResourceDefinition() {
        return McpResource.builder()
            .uri("prompt://system/{type}")
            .name("系统提示词")
            .description("""
                获取系统提示词模板。
                
                支持的类型：
                - react: ReAct (Reasoning + Acting) 提示词模板
                - assistant: 智能助手默认提示词
                
                这些提示词用于指导AI的行为和响应风格。
                """)
            .mimeType("text/plain")
            .metadata(Map.of(
                "category", "prompt",
                "version", "1.0"
            ))
            .build();
    }
    
    private String loadReactPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource(promptPath);
        if (resource.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
        return "ReAct 提示词文件未找到";
    }
    
    private String getAssistantPrompt() {
        return """
            你是一个智能助手，可以帮助用户查询天气、股票等信息。
            你拥有工具查询能力，并且能够利用对话历史中的信息来回答用户的后续问题。
            
            重要能力：
            1. 查询城市天气信息（get_weather 工具）
            2. 查询股票价格信息（get_stock 工具）
            3. 基于历史对话信息回答用户的后续问题
            
            关键规则：
            - 当用户问'适合什么活动'、'那明天呢'等后续问题时，要查看提供的历史对话上下文
            - 历史信息会在用户消息前以【历史上下文】标记提供给你
            - 充分利用这些历史信息来理解用户的意图并给出相关回答
            - 如果历史中有工具调用结果，要基于这些结果回答
            
            示例：
            【历史上下文】工具调用：get_weather，参数：鹰潭，结果：温度25°C，晴
            用户：适合什么活动？
            助手：根据鹰潭今天25°C的晴朗天气，非常适合户外活动，如爬山、骑行、野餐等。
            """;
    }
}








