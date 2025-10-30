package com.cuixing.aitestproject.chat;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final OllamaService ollamaService;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    public ChatWebSocketHandler(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket 连接已建立，会话ID: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userInput = message.getPayload();
        String sessionId = session.getId();
        
        System.out.println("收到消息 [会话 " + sessionId + "]: " + userInput);

        // 调用Ollama生成响应，传入 sessionId 以维护上下文
        String aiResponse = ollamaService.generateResponse(sessionId, userInput);

        // 发送回前端
        session.sendMessage(new TextMessage("{\"response\": \"" + aiResponse.replace("\"", "\\\"") + "\"}"));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(session);
        
        // 清除该会话的上下文记忆
        ollamaService.clearSessionContext(sessionId);
        System.out.println("WebSocket 连接已关闭，会话ID: " + sessionId + ", 状态: " + status);
    }
}