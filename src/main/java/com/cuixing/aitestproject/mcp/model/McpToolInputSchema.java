package com.cuixing.aitestproject.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 工具输入参数Schema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolInputSchema {
    /**
     * Schema 类型（通常为 "object"）
     */
    @Builder.Default
    private String type = "object";
    
    /**
     * 参数属性定义
     */
    private Map<String, PropertySchema> properties;
    
    /**
     * 必需参数列表
     */
    private List<String> required;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertySchema {
        /**
         * 参数类型：string, number, boolean, array, object
         */
        private String type;
        
        /**
         * 参数描述
         */
        private String description;
        
        /**
         * 枚举值（可选）
         */
        private List<String> enumValues;
        
        /**
         * 默认值（可选）
         */
        private Object defaultValue;
    }
}








