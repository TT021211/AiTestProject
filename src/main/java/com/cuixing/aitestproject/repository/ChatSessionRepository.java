package com.cuixing.aitestproject.repository;

import com.cuixing.aitestproject.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天会话数据访问层
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    /**
     * 根据会话ID查找
     */
    Optional<ChatSession> findBySessionId(String sessionId);
    
    /**
     * 查找所有活跃的会话
     */
    List<ChatSession> findByStatus(String status);
    
    /**
     * 查找指定时间之前的会话（用于清理）
     */
    @Query("SELECT s FROM ChatSession s WHERE s.lastActiveAt < :cutoffTime")
    List<ChatSession> findInactiveSessions(LocalDateTime cutoffTime);
    
    /**
     * 删除指定时间之前的会话
     */
    void deleteByLastActiveAtBefore(LocalDateTime cutoffTime);
}



