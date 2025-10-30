package com.cuixing.aitestproject.mcp.server;

import com.cuixing.aitestproject.mcp.core.McpResourceRegistry;
import com.cuixing.aitestproject.mcp.core.McpToolRegistry;
import com.cuixing.aitestproject.mcp.model.McpResource;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.protocol.McpRequest;
import com.cuixing.aitestproject.mcp.protocol.McpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MCP 服务器 - 处理 MCP 协议请求
 */
@Service
public class McpServer {
    private static final Logger log = LoggerFactory.getLogger(McpServer.class);
    
    private final McpToolRegistry toolRegistry;
    private final McpResourceRegistry resourceRegistry;
    
    // 服务器信息
    private final Map<String, Object> serverInfo;
    
    @Autowired
    public McpServer(McpToolRegistry toolRegistry, McpResourceRegistry resourceRegistry) {
        this.toolRegistry = toolRegistry;
        this.resourceRegistry = resourceRegistry;
        
        // 初始化服务器信息
        this.serverInfo = new HashMap<>();
        serverInfo.put("name", "AiTestProject-MCP-Server");
        serverInfo.put("version", "1.0.0");
        serverInfo.put("protocolVersion", "2024-11-05");
        serverInfo.put("capabilities", Map.of(
            "tools", Map.of("listChanged", true),
            "resources", Map.of("subscribe", true, "listChanged", true)
        ));
        
        log.info("MCP服务器初始化完成: {}", serverInfo);
    }
    
    /**
     * 处理 MCP 请求
     */
    public McpResponse<?> handleRequest(McpRequest request) {
        try {
            String method = request.getMethod();
            Map<String, Object> params = request.getParams();
            String id = request.getId();
            
            log.info("处理MCP请求: method={}, id={}", method, id);
            
            return switch (method) {
                case "initialize" -> handleInitialize(id);
                case "tools/list" -> handleToolsList(id);
                case "tools/call" -> handleToolsCall(id, params);
                case "resources/list" -> handleResourcesList(id);
                case "resources/read" -> handleResourcesRead(id, params);
                case "ping" -> McpResponse.success(id, Map.of("status", "pong"));
                default -> McpResponse.error(id, -32601, "方法未找到: " + method);
            };
            
        } catch (Exception e) {
            log.error("处理MCP请求失败", e);
            return McpResponse.error(request.getId(), -32603, "内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理 initialize 请求
     */
    private McpResponse<?> handleInitialize(String id) {
        return McpResponse.success(id, serverInfo);
    }
    
    /**
     * 处理 tools/list 请求
     */
    private McpResponse<?> handleToolsList(String id) {
        List<McpTool> tools = toolRegistry.listTools();
        Map<String, Object> result = Map.of(
            "tools", tools,
            "nextCursor", (Object) null // 如果需要分页，返回下一页游标
        );
        return McpResponse.success(id, result);
    }
    
    /**
     * 处理 tools/call 请求
     */
    private McpResponse<?> handleToolsCall(String id, Map<String, Object> params) {
        try {
            String toolName = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (toolName == null) {
                return McpResponse.error(id, -32602, "缺少工具名称");
            }
            
            Object result = toolRegistry.executeTool(toolName, arguments != null ? arguments : Map.of());
            
            // MCP 工具调用结果格式
            Map<String, Object> toolResult = Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", formatToolResult(result)
                )),
                "isError", false
            );
            
            return McpResponse.success(id, toolResult);
            
        } catch (Exception e) {
            log.error("工具调用失败", e);
            Map<String, Object> errorResult = Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "错误: " + e.getMessage()
                )),
                "isError", true
            );
            return McpResponse.success(id, errorResult);
        }
    }
    
    /**
     * 处理 resources/list 请求
     */
    private McpResponse<?> handleResourcesList(String id) {
        List<McpResource> resources = resourceRegistry.listResources();
        Map<String, Object> result = Map.of(
            "resources", resources,
            "nextCursor", (Object) null
        );
        return McpResponse.success(id, result);
    }
    
    /**
     * 处理 resources/read 请求
     */
    private McpResponse<?> handleResourcesRead(String id, Map<String, Object> params) {
        try {
            String uri = (String) params.get("uri");
            
            if (uri == null) {
                return McpResponse.error(id, -32602, "缺少资源URI");
            }
            
            Object content = resourceRegistry.readResource(uri);
            McpResource resource = resourceRegistry.getResource(uri);
            
            Map<String, Object> result = Map.of(
                "contents", List.of(Map.of(
                    "uri", uri,
                    "mimeType", resource != null ? resource.getMimeType() : "text/plain",
                    "text", content.toString()
                ))
            );
            
            return McpResponse.success(id, result);
            
        } catch (Exception e) {
            log.error("资源读取失败", e);
            return McpResponse.error(id, -32603, "资源读取失败: " + e.getMessage());
        }
    }
    
    /**
     * 格式化工具执行结果
     */
    private String formatToolResult(Object result) {
        if (result == null) {
            return "null";
        }
        
        if (result instanceof Map) {
            // 格式化为JSON字符串
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = 
                    new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            } catch (Exception e) {
                return result.toString();
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取服务器信息
     */
    public Map<String, Object> getServerInfo() {
        return new HashMap<>(serverInfo);
    }
}

