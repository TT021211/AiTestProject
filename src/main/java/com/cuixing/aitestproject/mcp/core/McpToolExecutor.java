package com.cuixing.aitestproject.mcp.core;

import com.cuixing.aitestproject.mcp.model.McpTool;

import java.util.Map;

/**
 * MCP 工具执行器接口
 */
@FunctionalInterface
public interface McpToolExecutor {
    /**
     * 执行工具调用
     * 
     * @param toolName 工具名称
     * @param arguments 参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    Object execute(String toolName, Map<String, Object> arguments) throws Exception;
    
    /**
     * 获取工具定义（可选实现）
     */
    default McpTool getToolDefinition() {
        return null;
    }
}








