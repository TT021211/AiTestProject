package com.cuixing.aitestproject.mcp.controller;

import com.cuixing.aitestproject.mcp.protocol.McpRequest;
import com.cuixing.aitestproject.mcp.protocol.McpResponse;
import com.cuixing.aitestproject.mcp.server.McpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP HTTP API 控制器
 * 提供符合MCP协议的HTTP接口
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class McpController {
    private static final Logger log = LoggerFactory.getLogger(McpController.class);
    
    private final McpServer mcpServer;
    
    @Autowired
    public McpController(McpServer mcpServer) {
        this.mcpServer = mcpServer;
    }
    
    /**
     * MCP 协议入口
     * POST /api/mcp
     */
    @PostMapping
    public ResponseEntity<McpResponse<?>> handleMcpRequest(@RequestBody McpRequest request) {
        log.info("收到MCP请求: method={}, id={}", request.getMethod(), request.getId());
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取服务器信息
     * GET /api/mcp/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        return ResponseEntity.ok(mcpServer.getServerInfo());
    }
    
    /**
     * 健康检查
     * GET /api/mcp/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "protocol", "MCP",
            "version", "1.0.0"
        ));
    }
}








