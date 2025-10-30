package com.cuixing.aitestproject.repository;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息数据访问层
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * 查找指定会话的所有消息
     */
    List<ChatMessage> findBySessionOrderByTimestampAsc(ChatSession session);
    
    /**
     * 查找指定会话的最近N条消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session = :session ORDER BY m.timestamp DESC")
    List<ChatMessage> findRecentMessagesBySession(@Param("session") ChatSession session);
    
    /**
     * 查找指定会话在指定时间范围内的消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session = :session AND m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp ASC")
    List<ChatMessage> findMessagesBySessionAndTimeRange(
        @Param("session") ChatSession session,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 统计指定会话的消息数量
     */
    long countBySession(ChatSession session);
    
    /**
     * 删除指定会话的所有消息
     */
    void deleteBySession(ChatSession session);
}



