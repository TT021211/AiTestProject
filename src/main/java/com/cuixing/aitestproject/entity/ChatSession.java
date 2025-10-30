package com.cuixing.aitestproject.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话实体
 * 用于存储每个 WebSocket 会话的基本信息
 */
@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * WebSocket 会话ID
     */
    @Column(nullable = false, unique = true, length = 100)
    private String sessionId;
    
    /**
     * 会话创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 会话最后活跃时间
     */
    @Column(nullable = false)
    private LocalDateTime lastActiveAt;
    
    /**
     * 会话状态：active, closed
     */
    @Column(nullable = false, length = 20)
    private String status;
    
    /**
     * 该会话的所有消息
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActiveAt = LocalDateTime.now();
    }
}

