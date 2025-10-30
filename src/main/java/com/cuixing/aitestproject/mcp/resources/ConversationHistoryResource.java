package com.cuixing.aitestproject.mcp.resources;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.mcp.core.McpResourceProvider;
import com.cuixing.aitestproject.mcp.model.McpResource;
import com.cuixing.aitestproject.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP 资源 - 对话历史
 * 提供会话的对话历史记录访问
 */
@Component
public class ConversationHistoryResource implements McpResourceProvider {
    
    private final RagService ragService;
    
    @Autowired
    public ConversationHistoryResource(RagService ragService) {
        this.ragService = ragService;
    }
    
    @Override
    public Object readResource(String uri) throws Exception {
        // URI 格式: conversation://history/{sessionId}?limit=10
        String[] parts = uri.replace("conversation://history/", "").split("\\?");
        String sessionId = parts[0];
        
        // 解析查询参数
        int limit = 20; // 默认20条
        if (parts.length > 1) {
            String[] params = parts[1].split("&");
            for (String param : params) {
                if (param.startsWith("limit=")) {
                    limit = Integer.parseInt(param.substring(6));
                }
            }
        }
        
        // 获取对话历史
        List<ChatMessage> messages = ragService.getConversationHistory(sessionId, limit);
        
        // 格式化为可读文本
        StringBuilder sb = new StringBuilder();
        sb.append("=== 对话历史 (会话: ").append(sessionId).append(") ===\n\n");
        
        for (ChatMessage msg : messages) {
            sb.append("[").append(msg.getTimestamp()).append("] ");
            sb.append(formatMessageType(msg.getMessageType())).append(": ");
            
            if (msg.getContent() != null) {
                sb.append(msg.getContent());
            } else if (msg.getToolName() != null) {
                sb.append("工具调用 - ").append(msg.getToolName());
                if (msg.getToolResult() != null) {
                    sb.append(" => ").append(msg.getToolResult());
                }
            }
            
            sb.append("\n\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public McpResource getResourceDefinition() {
        return McpResource.builder()
            .uri("conversation://history/{sessionId}")
            .name("对话历史")
            .description("""
                获取指定会话的对话历史记录。
                
                URI格式: conversation://history/{sessionId}?limit=N
                
                参数：
                - sessionId: 会话ID（必需）
                - limit: 返回的消息数量（可选，默认20）
                
                返回格式：
                按时间顺序排列的对话记录，包括用户消息、助手回复和工具调用结果。
                """)
            .mimeType("text/plain")
            .metadata(Map.of(
                "category", "conversation",
                "dynamic", true,
                "version", "1.0"
            ))
            .build();
    }
    
    private String formatMessageType(String type) {
        return switch (type) {
            case "user" -> "用户";
            case "assistant" -> "助手";
            case "tool_result" -> "工具";
            default -> type;
        };
    }
}

