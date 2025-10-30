package com.cuixing.aitestproject.mcp.config;

import com.cuixing.aitestproject.chat.McpChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * MCP WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class McpWebSocketConfig implements WebSocketConfigurer {
    
    private final McpChatWebSocketHandler mcpChatWebSocketHandler;
    
    @Autowired
    public McpWebSocketConfig(McpChatWebSocketHandler mcpChatWebSocketHandler) {
        this.mcpChatWebSocketHandler = mcpChatWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // 注册MCP WebSocket端点
        registry.addHandler(mcpChatWebSocketHandler, "/ws/mcp-chat")
                .setAllowedOrigins("*");
    }
}

