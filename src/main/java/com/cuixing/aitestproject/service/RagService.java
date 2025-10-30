package com.cuixing.aitestproject.service;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.entity.ChatSession;
import com.cuixing.aitestproject.entity.VectorEmbedding;
import com.cuixing.aitestproject.repository.ChatMessageRepository;
import com.cuixing.aitestproject.repository.ChatSessionRepository;
import com.cuixing.aitestproject.repository.VectorEmbeddingRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) 服务
 * 负责向量嵌入的生成、存储和检索
 */
@Service
@Transactional
public class RagService {
    
    @Autowired
    private ChatSessionRepository sessionRepository;
    
    @Autowired
    private ChatMessageRepository messageRepository;
    
    @Autowired
    private VectorEmbeddingRepository embeddingRepository;
    
    private final EmbeddingModel embeddingModel;
    
    public RagService() {
        // 初始化嵌入模型（384维向量）
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        System.out.println("RAG Service 初始化完成，嵌入模型已加载");
    }
    
    /**
     * 获取或创建会话
     */
    public ChatSession getOrCreateSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession session = ChatSession.builder()
                            .sessionId(sessionId)
                            .status("active")
                            .build();
                    return sessionRepository.save(session);
                });
    }
    
    /**
     * 保存用户消息并生成向量 (Java 17 优化)
     */
    public ChatMessage saveUserMessage(String sessionId, String content) {
        var session = getOrCreateSession(sessionId);
        
        var message = ChatMessage.builder()
                .session(session)
                .messageType("user")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        
        var savedMessage = messageRepository.save(message);
        
        // 生成并保存向量
        generateAndSaveEmbedding(savedMessage);
        
        return savedMessage;
    }
    
    /**
     * 保存AI回复并生成向量
     */
    public ChatMessage saveAssistantMessage(String sessionId, String content) {
        ChatSession session = getOrCreateSession(sessionId);
        
        ChatMessage message = ChatMessage.builder()
                .session(session)
                .messageType("assistant")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        
        message = messageRepository.save(message);
        
        // 生成并保存向量
        generateAndSaveEmbedding(message);
        
        return message;
    }
    
    /**
     * 保存工具调用记录并生成向量
     */
    public ChatMessage saveToolCall(String sessionId, String toolName, String toolParams, String toolResult) {
        ChatSession session = getOrCreateSession(sessionId);
        
        // 构建富含语义的内容用于向量化
        String semanticContent = String.format(
            "工具调用：%s，参数：%s，结果：%s",
            toolName, toolParams, toolResult
        );
        
        ChatMessage message = ChatMessage.builder()
                .session(session)
                .messageType("tool_result")
                .content(semanticContent)
                .toolName(toolName)
                .toolParams(toolParams)
                .toolResult(toolResult)
                .timestamp(LocalDateTime.now())
                .build();
        
        message = messageRepository.save(message);
        
        // 生成并保存向量
        generateAndSaveEmbedding(message);
        
        return message;
    }
    
    /**
     * 为消息生成向量嵌入并保存
     */
    private void generateAndSaveEmbedding(ChatMessage message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return;
        }
        
        try {
            // 生成向量
            Embedding embedding = embeddingModel.embed(message.getContent()).content();
            float[] vector = embedding.vector();
            
            // 保存到数据库
            VectorEmbedding vectorEmbedding = VectorEmbedding.builder()
                    .message(message)
                    .dimension(vector.length)
                    .vectorData(VectorEmbedding.vectorToString(vector))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            embeddingRepository.save(vectorEmbedding);
            
        } catch (Exception e) {
            System.err.println("生成向量嵌入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检索与查询最相关的历史消息（基于向量相似度）
     */
    public List<ChatMessage> retrieveRelevantMessages(String sessionId, String query, int topK) {
        try {
            // 1. 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            float[] queryVector = queryEmbedding.vector();
            
            // 2. 获取该会话的所有向量
            List<VectorEmbedding> allEmbeddings = embeddingRepository.findBySessionId(sessionId);
            
            if (allEmbeddings.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 3. 计算相似度并排序
            List<ScoredMessage> scoredMessages = allEmbeddings.stream()
                    .map(embedding -> {
                        float[] docVector = VectorEmbedding.stringToVector(embedding.getVectorData());
                        double similarity = VectorEmbedding.cosineSimilarity(queryVector, docVector);
                        
                        // 在事务内立即访问所有需要的属性，避免 LazyInitializationException
                        ChatMessage msg = embedding.getMessage();
                        // 触发懒加载
                        msg.getMessageType();
                        msg.getContent();
                        msg.getToolName();
                        msg.getToolResult();
                        
                        return new ScoredMessage(msg, similarity);
                    })
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .limit(topK)
                    .collect(Collectors.toList());
            
            // 4. 返回消息列表
            return scoredMessages.stream()
                    .map(sm -> sm.message)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("检索相关消息失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取会话的对话历史（按时间顺序）
     */
    public List<ChatMessage> getConversationHistory(String sessionId, int limit) {
        ChatSession session = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (session == null) {
            return Collections.emptyList();
        }
        
        List<ChatMessage> messages = messageRepository.findRecentMessagesBySession(session);
        
        // 限制返回数量
        if (messages.size() > limit) {
            messages = messages.subList(0, limit);
        }
        
        // 反转顺序，使其按时间正序排列
        Collections.reverse(messages);
        
        return messages;
    }
    
    /**
     * 清除会话的所有数据
     */
    public void clearSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            // 删除消息会级联删除向量
            messageRepository.deleteBySession(session);
            session.setStatus("closed");
            sessionRepository.save(session);
            System.out.println("已清除会话数据: " + sessionId);
        });
    }
    
    /**
     * 清理不活跃的会话（超过指定时间未活动）
     */
    public void cleanupInactiveSessions(int hoursInactive) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursInactive);
        List<ChatSession> inactiveSessions = sessionRepository.findInactiveSessions(cutoffTime);
        
        for (ChatSession session : inactiveSessions) {
            clearSession(session.getSessionId());
        }
        
        System.out.println("清理了 " + inactiveSessions.size() + " 个不活跃会话");
    }
    
    /**
     * 用于存储消息和相似度分数的内部类
     */
    private static class ScoredMessage {
        ChatMessage message;
        double score;
        
        ScoredMessage(ChatMessage message, double score) {
            this.message = message;
            this.score = score;
        }
    }
}

