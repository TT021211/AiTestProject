package com.cuixing.aitestproject.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 * 存储所有的对话消息，包括用户消息、AI回复、工具调用结果
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属会话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;
    
    /**
     * 消息类型：user, assistant, tool_call, tool_result
     */
    @Column(nullable = false, length = 20)
    private String messageType;
    
    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 工具名称（仅当 messageType = tool_call 或 tool_result 时）
     */
    @Column(length = 50)
    private String toolName;
    
    /**
     * 工具参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String toolParams;
    
    /**
     * 工具结果（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String toolResult;
    
    /**
     * 消息时间戳
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 该消息对应的向量嵌入
     */
    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private VectorEmbedding vectorEmbedding;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

