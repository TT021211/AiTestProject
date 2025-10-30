package com.cuixing.aitestproject.mcp.service;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.mcp.config.McpPromptConfig;
import com.cuixing.aitestproject.mcp.core.McpToolRegistry;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.service.RagService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP集成的Ollama服务
 * 使用MCP协议管理工具调用
 * 
 * 💬 提示词调整：注入 McpPromptConfig 来自定义提示词
 */
@Service
public class McpOllamaService {
    private static final Logger log = LoggerFactory.getLogger(McpOllamaService.class);
    
    private final ChatLanguageModel model;
    private final McpToolRegistry toolRegistry;
    private final RagService ragService;
    private final McpPromptConfig promptConfig;  // 💬 提示词配置
    
    // 工具调用匹配模式
    private static final Pattern TOOL_CALL_PATTERN = 
        Pattern.compile("\\[TOOL:(\\w+)\\]\\s*\\{([^}]*)\\}");
    
    @Autowired
    public McpOllamaService(McpToolRegistry toolRegistry, RagService ragService, McpPromptConfig promptConfig) {
        this.toolRegistry = toolRegistry;
        this.ragService = ragService;
        this.promptConfig = promptConfig;  // 💬 注入提示词配置
        
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3:8b")
                .temperature(0.3)
                .build();
        
        log.info("McpOllamaService 初始化完成 - MCP架构");
    }
    
    /**
     * 生成响应（MCP增强版本）
     */
    @Transactional
    public String generateResponse(String sessionId, String userMessage) {
        try {
            // 1. 保存用户消息
            ragService.saveUserMessage(sessionId, userMessage);
            
            // 2. 检索相关历史消息
            List<ChatMessage> relevantMessages = 
                ragService.retrieveRelevantMessages(sessionId, userMessage, 5);
            
            // 3. 构建增强的提示词（包含可用工具信息）
            String enhancedPrompt = buildMcpPrompt(userMessage, relevantMessages);
            
            // 4. 调用LLM生成回复
            String aiResponse = model.generate(enhancedPrompt);
            
            // 5. 检测并执行工具调用
            String processedResponse = processToolCalls(sessionId, aiResponse);
            
            // 6. 保存AI回复
            ragService.saveAssistantMessage(sessionId, processedResponse);
            
            return processedResponse;
            
        } catch (Exception e) {
            log.error("生成响应失败", e);
            return "抱歉，处理出错: " + e.getMessage();
        }
    }
    
    /**
     * 构建MCP增强的提示词
     * 
     * 💬 提示词调整：修改 McpPromptConfig 来自定义系统提示
     */
    private String buildMcpPrompt(String userMessage, List<ChatMessage> relevantMessages) {
        StringBuilder prompt = new StringBuilder();
        
        // 💬 系统指令 - 使用配置的提示词
        prompt.append(promptConfig.getSystemPrompt()).append("\n\n");
        
        // 添加可用工具列表
        List<McpTool> tools = toolRegistry.listTools();
        if (!tools.isEmpty()) {
            prompt.append("【可用工具】\n");
            for (McpTool tool : tools) {
                prompt.append("- ").append(tool.getName()).append(": ")
                      .append(tool.getDescription().split("\n")[0])
                      .append("\n");
            }
            prompt.append("\n");
            
            prompt.append("""
                工具调用格式：[TOOL:工具名] {参数JSON}
                例如：[TOOL:get_weather] {"city": "北京"}
                
                """);
        }
        
        // 添加历史上下文
        if (relevantMessages != null && !relevantMessages.isEmpty()) {
            prompt.append("【历史上下文】\n");
            for (ChatMessage msg : relevantMessages) {
                switch (msg.getMessageType()) {
                    case "user" -> prompt.append("用户: ").append(msg.getContent()).append("\n");
                    case "assistant" -> prompt.append("助手: ").append(msg.getContent()).append("\n");
                    case "tool_result" -> {
                        if (msg.getToolName() != null && msg.getToolResult() != null) {
                            prompt.append("工具[").append(msg.getToolName())
                                  .append("]: ").append(msg.getToolResult()).append("\n");
                        }
                    }
                }
            }
            prompt.append("\n");
        }
        
        // 当前用户问题
        prompt.append("【当前问题】\n");
        prompt.append("用户: ").append(userMessage).append("\n\n");
        prompt.append("助手: ");
        
        return prompt.toString();
    }
    
    /**
     * 处理工具调用
     */
    private String processToolCalls(String sessionId, String response) {
        Matcher matcher = TOOL_CALL_PATTERN.matcher(response);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String toolName = matcher.group(1);
            String argsJson = matcher.group(2);
            
            try {
                // 解析参数
                Map<String, Object> arguments = parseToolArguments(argsJson);
                
                // 执行工具
                Object toolResult = toolRegistry.executeTool(toolName, arguments);
                
                // 保存工具调用结果 (需要4个参数: sessionId, toolName, toolParams, toolResult)
                ragService.saveToolCall(sessionId, toolName, argsJson, toolResult.toString());
                
                // 替换为工具结果
                String resultText = formatToolResult(toolName, toolResult);
                matcher.appendReplacement(result, Matcher.quoteReplacement(resultText));
                
            } catch (Exception e) {
                log.error("工具调用失败: " + toolName, e);
                String errorText = "工具调用失败: " + e.getMessage();
                matcher.appendReplacement(result, Matcher.quoteReplacement(errorText));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 解析工具参数
     */
    private Map<String, Object> parseToolArguments(String argsJson) {
        Map<String, Object> args = new HashMap<>();
        
        // 简单的JSON解析（生产环境使用Jackson）
        argsJson = argsJson.trim();
        if (argsJson.isEmpty()) {
            return args;
        }
        
        String[] pairs = argsJson.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");
                String value = kv[1].trim().replaceAll("\"", "");
                args.put(key, value);
            }
        }
        
        return args;
    }
    
    /**
     * 格式化工具结果
     */
    private String formatToolResult(String toolName, Object result) {
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            
            // 特殊格式化天气结果
            if (toolName.equals("get_weather") && map.containsKey("temp")) {
                return String.format("天气信息：%s，温度%s°C，%s，湿度%s%%",
                    map.get("city"),
                    map.get("temp"),
                    map.get("text"),
                    map.get("humidity")
                );
            }
            
            // 特殊格式化股票结果
            if (toolName.equals("get_stock") && map.containsKey("price")) {
                return String.format("股票 %s 当前价格：$%s，涨跌幅：%s",
                    map.get("ticker"),
                    map.get("price"),
                    map.get("change")
                );
            }
        }
        
        return result.toString();
    }
    
    /**
     * 清除会话上下文
     */
    public void clearSessionContext(String sessionId) {
        ragService.clearSession(sessionId);
        log.info("已清除会话 {} 的所有数据（MCP）", sessionId);
    }
    
    /**
     * 获取对话历史
     */
    public List<ChatMessage> getConversationHistory(String sessionId, int limit) {
        return ragService.getConversationHistory(sessionId, limit);
    }
    
    /**
     * 获取可用工具列表
     */
    public List<McpTool> getAvailableTools() {
        return toolRegistry.listTools();
    }
}

