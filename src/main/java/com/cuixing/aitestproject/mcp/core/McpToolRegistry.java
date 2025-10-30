package com.cuixing.aitestproject.mcp.core;

import com.cuixing.aitestproject.mcp.model.McpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 工具注册中心
 */
@Component
public class McpToolRegistry {
    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);
    
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    private final Map<String, McpToolExecutor> executors = new ConcurrentHashMap<>();
    
    /**
     * 注册工具
     */
    public void registerTool(McpTool tool, McpToolExecutor executor) {
        if (tool == null || tool.getName() == null) {
            throw new IllegalArgumentException("工具或工具名称不能为空");
        }
        
        tools.put(tool.getName(), tool);
        executors.put(tool.getName(), executor);
        log.info("MCP工具已注册: {} - {}", tool.getName(), tool.getDescription());
    }
    
    /**
     * 注销工具
     */
    public void unregisterTool(String toolName) {
        tools.remove(toolName);
        executors.remove(toolName);
        log.info("MCP工具已注销: {}", toolName);
    }
    
    /**
     * 获取所有工具定义
     */
    public List<McpTool> listTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 获取工具定义
     */
    public McpTool getTool(String toolName) {
        return tools.get(toolName);
    }
    
    /**
     * 执行工具调用
     */
    public Object executeTool(String toolName, Map<String, Object> arguments) throws Exception {
        McpToolExecutor executor = executors.get(toolName);
        if (executor == null) {
            throw new IllegalArgumentException("未找到工具: " + toolName);
        }
        
        log.info("执行MCP工具: {} with arguments: {}", toolName, arguments);
        return executor.execute(toolName, arguments);
    }
    
    /**
     * 检查工具是否存在
     */
    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
    
    /**
     * 获取工具数量
     */
    public int getToolCount() {
        return tools.size();
    }
}








