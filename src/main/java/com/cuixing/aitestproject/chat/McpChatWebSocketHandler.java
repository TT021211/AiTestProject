package com.cuixing.aitestproject.chat;

import com.cuixing.aitestproject.mcp.service.McpOllamaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP WebSocket 处理器
 * 支持MCP协议的WebSocket聊天
 */
@Component
public class McpChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(McpChatWebSocketHandler.class);
    
    private final McpOllamaService mcpOllamaService;
    private final ObjectMapper objectMapper;
    
    // 存储 WebSocket Session ID 到 Chat Session ID 的映射
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();
    
    @Autowired
    public McpChatWebSocketHandler(McpOllamaService mcpOllamaService) {
        this.mcpOllamaService = mcpOllamaService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String wsSessionId = session.getId();
        // 为每个WebSocket连接分配一个聊天会话ID
        String chatSessionId = "ws-" + wsSessionId;
        sessionMap.put(wsSessionId, chatSessionId);
        
        log.info("MCP WebSocket 连接建立: wsId={}, chatId={}", wsSessionId, chatSessionId);
        
        // 发送欢迎消息
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
            "type", "system",
            "message", "欢迎使用MCP智能助手！我可以帮您查询天气、股票等信息。",
            "sessionId", chatSessionId,
            "availableTools", mcpOllamaService.getAvailableTools().stream()
                .map(tool -> Map.of(
                    "name", tool.getName(),
                    "description", tool.getDescription().split("\n")[0]
                ))
                .toList()
        ))));
    }
    
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String wsSessionId = session.getId();
        String chatSessionId = sessionMap.get(wsSessionId);
        
        if (chatSessionId == null) {
            log.warn("未找到会话映射: {}", wsSessionId);
            return;
        }
        
        String payload = message.getPayload();
        log.info("收到消息: session={}, message={}", chatSessionId, payload);
        
        try {
            // 解析消息
            JsonNode json = objectMapper.readTree(payload);
            String messageType = json.path("type").asText("text");
            
            switch (messageType) {
                case "text" -> handleTextChat(session, chatSessionId, json);
                case "clear" -> handleClearSession(session, chatSessionId);
                case "history" -> handleGetHistory(session, chatSessionId, json);
                default -> {
                    log.warn("未知消息类型: {}", messageType);
                    sendError(session, "未知消息类型: " + messageType);
                }
            }
            
        } catch (Exception e) {
            log.error("处理消息失败", e);
            sendError(session, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理文本聊天
     */
    private void handleTextChat(WebSocketSession session, String chatSessionId, JsonNode json) throws Exception {
        String userMessage = json.path("message").asText();
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            sendError(session, "消息不能为空");
            return;
        }
        
        // 发送处理中状态
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
            "type", "status",
            "status", "processing",
            "message", "正在处理您的请求..."
        ))));
        
        // 调用MCP服务生成响应
        String aiResponse = mcpOllamaService.generateResponse(chatSessionId, userMessage);
        
        // 发送AI响应
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
            "type", "response",
            "message", aiResponse,
            "sessionId", chatSessionId
        ))));
    }
    
    /**
     * 处理清除会话
     */
    private void handleClearSession(WebSocketSession session, String chatSessionId) throws Exception {
        mcpOllamaService.clearSessionContext(chatSessionId);
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
            "type", "system",
            "message", "会话已清除"
        ))));
    }
    
    /**
     * 处理获取历史记录
     */
    private void handleGetHistory(WebSocketSession session, String chatSessionId, JsonNode json) throws Exception {
        int limit = json.path("limit").asInt(20);
        
        var history = mcpOllamaService.getConversationHistory(chatSessionId, limit);
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
            "type", "history",
            "messages", history
        ))));
    }
    
    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "error",
                "message", errorMessage
            ))));
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }
    
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String wsSessionId = session.getId();
        String chatSessionId = sessionMap.remove(wsSessionId);
        
        log.info("MCP WebSocket 连接关闭: wsId={}, chatId={}, status={}", 
            wsSessionId, chatSessionId, status);
    }
    
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("MCP WebSocket 传输错误: sessionId={}", session.getId(), exception);
    }
}

