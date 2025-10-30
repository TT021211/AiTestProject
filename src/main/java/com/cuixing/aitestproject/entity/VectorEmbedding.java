package com.cuixing.aitestproject.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 向量嵌入实体
 * 存储消息的向量表示，用于语义检索
 */
@Entity
@Table(name = "vector_embeddings", indexes = {
    @Index(name = "idx_message_id", columnList = "message_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorEmbedding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的消息
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, unique = true)
    private ChatMessage message;
    
    /**
     * 向量维度（例如：384 for all-MiniLM-L6-v2）
     */
    @Column(nullable = false)
    private Integer dimension;
    
    /**
     * 向量数据（以逗号分隔的浮点数字符串）
     * 注意：生产环境中应该使用专门的向量数据库（如 PostgreSQL + pgvector）
     * 这里为了演示使用 TEXT 字段存储
     */
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String vectorData;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * 将向量数组转换为字符串存储 (使用 Java 17 优化)
     */
    public static String vectorToString(float[] vector) {
        var sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        return sb.toString();
    }
    
    /**
     * 将字符串转换回向量数组 (使用 Java 17 Stream API)
     */
    public static float[] stringToVector(String vectorString) {
        var parts = vectorString.split(",");
        var vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }
    
    /**
     * 计算两个向量的余弦相似度
     */
    public static double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}

