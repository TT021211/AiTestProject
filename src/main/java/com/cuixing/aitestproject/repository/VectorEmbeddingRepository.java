package com.cuixing.aitestproject.repository;

import com.cuixing.aitestproject.entity.ChatMessage;
import com.cuixing.aitestproject.entity.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 向量嵌入数据访问层
 */
@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, Long> {
    
    /**
     * 根据消息ID查找向量
     */
    Optional<VectorEmbedding> findByMessage(ChatMessage message);
    
    /**
     * 查找指定会话的所有向量
     */
    @Query("SELECT v FROM VectorEmbedding v WHERE v.message.session.sessionId = :sessionId")
    List<VectorEmbedding> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 查找所有向量（用于相似度搜索）
     * 注意：实际应用中应该分页或限制数量
     */
    @Query("SELECT v FROM VectorEmbedding v WHERE v.message.session.sessionId = :sessionId ORDER BY v.createdAt DESC")
    List<VectorEmbedding> findAllBySessionIdOrderByCreatedAtDesc(@Param("sessionId") String sessionId);
}



