package com.cuixing.aitestproject.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 资源定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResource {
    /**
     * 资源URI（唯一标识）
     */
    private String uri;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源描述
     */
    private String description;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 资源元数据
     */
    private Map<String, Object> metadata;
}








