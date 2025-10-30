package com.cuixing.aitestproject.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 工具定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpTool {
    /**
     * 工具名称（唯一标识）
     */
    private String name;
    
    /**
     * 工具描述
     */
    private String description;
    
    /**
     * 输入参数Schema（JSON Schema格式）
     */
    private McpToolInputSchema inputSchema;
    
    /**
     * 工具元数据
     */
    private Map<String, Object> metadata;
}


