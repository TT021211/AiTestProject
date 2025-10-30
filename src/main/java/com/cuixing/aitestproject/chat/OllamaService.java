package com.cuixing.aitestproject.chat;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.service.RagService;
import com.cuixing.aitestproject.tool.StockTool;
import com.cuixing.aitestproject.tool.WeatherTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama 服务 - 使用 RAG（检索增强生成）
 * 集成向量数据库实现持久化的上下文管理
 */
@Service
public class OllamaService {
    private final ChatLanguageModel model;
    private final WeatherTool weatherTool;
    private final StockTool stockTool;
    private final RagService ragService;
    
    // 为每个会话存储 AI 助手实例
    private final Map<String, Assistant> sessionAssistants = new HashMap<>();

    @Autowired
    public OllamaService(WeatherTool weatherTool, StockTool stockTool, RagService ragService) {
        this.weatherTool = weatherTool;
        this.stockTool = stockTool;
        this.ragService = ragService;
        
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3:8b")
                .temperature(0.3) // 提高一点创造性
                .build();
        
        System.out.println("OllamaService 初始化完成 - 使用 RAG 增强");
    }

    // 定义智能助手接口 (使用 Java 17 Text Blocks)
    interface Assistant {
        @SystemMessage("""
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
            """)
        String chat(@UserMessage String message);
    }

    // 为指定会话创建或获取 Assistant
    private Assistant getOrCreateAssistant(String sessionId) {
        return sessionAssistants.computeIfAbsent(sessionId, id -> {
            System.out.println("为会话 " + sessionId + " 创建新的 AI Assistant (RAG 模式)");
            
            return AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .tools(weatherTool, stockTool)
                    .build();
        });
    }

    /**
     * 使用 RAG 生成响应
     * 1. 保存用户消息到数据库
     * 2. 检索相关的历史消息
     * 3. 构建增强的提示词
     * 4. 调用 AI 生成回复
     * 5. 保存 AI 回复和工具调用结果到数据库
     */
    @Transactional
    public String generateResponse(String sessionId, String userMessage) {
        try {
            // 1. 保存用户消息
            ragService.saveUserMessage(sessionId, userMessage);
            
            // 2. 检索相关的历史消息（基于向量相似度）
            List<ChatMessage> relevantMessages = ragService.retrieveRelevantMessages(sessionId, userMessage, 5);
            
            // 3. 构建增强的上下文
            String enhancedPrompt = buildEnhancedPrompt(userMessage, relevantMessages);
            
            // 4. 获取 AI 助手并生成回复
            Assistant assistant = getOrCreateAssistant(sessionId);
            String aiResponse = assistant.chat(enhancedPrompt);
            
            // 5. 保存 AI 回复
            ragService.saveAssistantMessage(sessionId, aiResponse);
            
            // 6. 检查并保存工具调用结果（如果有）
            // 注意：工具调用由 LangChain4j 自动处理，我们需要拦截结果
            // 这部分会在后续优化中实现
            
            return aiResponse;
            
        } catch (Exception e) {
            System.err.println("生成响应失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，处理出错: " + e.getMessage();
        }
    }

    /**
     * 构建包含历史上下文的增强提示词 (使用 Java 17 switch expression)
     */
    private String buildEnhancedPrompt(String userMessage, List<ChatMessage> relevantMessages) {
        if (relevantMessages == null || relevantMessages.isEmpty()) {
            return userMessage;
        }
        
        var contextBuilder = new StringBuilder();
        contextBuilder.append("【历史上下文】\n");
        
        for (var msg : relevantMessages) {
            try {
                String messageType = msg.getMessageType();
                if (messageType == null) continue;
                
                // 使用 Java 17 switch expression
                switch (messageType) {
                    case "user" -> {
                        var content = msg.getContent();
                        if (content != null) {
                            contextBuilder.append("用户之前问过: ").append(content).append("\n");
                        }
                    }
                    case "assistant" -> {
                        var content = msg.getContent();
                        if (content != null) {
                            contextBuilder.append("你之前回答过: ").append(content).append("\n");
                        }
                    }
                    case "tool_result" -> {
                        var toolName = msg.getToolName();
                        var toolResult = msg.getToolResult();
                        if (toolName != null && toolResult != null) {
                            contextBuilder.append("工具调用结果: ")
                                          .append(toolName)
                                          .append(" - ")
                                          .append(toolResult)
                                          .append("\n");
                        }
                    }
                    default -> { /* 忽略未知类型 */ }
                }
            } catch (Exception e) {
                System.err.println("处理历史消息时出错: " + e.getMessage());
                // 跳过有问题的消息，继续处理其他消息
            }
        }
        
        contextBuilder.append("\n【当前问题】\n");
        contextBuilder.append(userMessage);
        
        return contextBuilder.toString();
    }

    /**
     * 清除指定会话的上下文
     */
    public void clearSessionContext(String sessionId) {
        sessionAssistants.remove(sessionId);
        ragService.clearSession(sessionId);
        System.out.println("已清除会话 " + sessionId + " 的所有数据（包括向量）");
    }

    /**
     * 获取当前活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessionAssistants.size();
    }
    
    /**
     * 获取会话的对话历史
     */
    public List<ChatMessage> getConversationHistory(String sessionId, int limit) {
        return ragService.getConversationHistory(sessionId, limit);
    }
}