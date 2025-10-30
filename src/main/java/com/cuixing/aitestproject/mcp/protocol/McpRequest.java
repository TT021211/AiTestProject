package com.cuixing.aitestproject.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 请求对象 - 符合 Model Context Protocol 标准
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpRequest {
    /**
     * 请求类型：tools/list, tools/call, resources/list, resources/read, prompts/list
     */
    private String method;
    
    /**
     * 请求参数
     */
    private Map<String, Object> params;
    
    /**
     * 请求ID（用于异步响应匹配）
     */
    private String id;
    
    /**
     * 协议版本
     */
    @Builder.Default
    private String jsonrpc = "2.0";
}


