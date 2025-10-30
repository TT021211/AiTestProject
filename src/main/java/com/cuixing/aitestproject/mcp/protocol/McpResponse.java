package com.cuixing.aitestproject.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 响应对象 - 符合 Model Context Protocol 标准
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse<T> {
    /**
     * 协议版本
     */
    @Builder.Default
    private String jsonrpc = "2.0";
    
    /**
     * 请求ID（与请求匹配）
     */
    private String id;
    
    /**
     * 成功响应数据
     */
    private T result;
    
    /**
     * 错误信息（如果有）
     */
    private McpError error;
    
    /**
     * 创建成功响应
     */
    public static <T> McpResponse<T> success(String id, T result) {
        return McpResponse.<T>builder()
                .id(id)
                .result(result)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static <T> McpResponse<T> error(String id, int code, String message) {
        return McpResponse.<T>builder()
                .id(id)
                .error(new McpError(code, message, null))
                .build();
    }
}


