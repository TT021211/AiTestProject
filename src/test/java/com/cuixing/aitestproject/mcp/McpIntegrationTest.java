package com.cuixing.aitestproject.mcp;

import com.cuixing.aitestproject.mcp.core.McpToolRegistry;
import com.cuixing.aitestproject.mcp.core.McpResourceRegistry;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpResource;
import com.cuixing.aitestproject.mcp.protocol.McpRequest;
import com.cuixing.aitestproject.mcp.protocol.McpResponse;
import com.cuixing.aitestproject.mcp.server.McpServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MCP 集成测试
 */
@SpringBootTest
public class McpIntegrationTest {
    
    @Autowired
    private McpServer mcpServer;
    
    @Autowired
    private McpToolRegistry toolRegistry;
    
    @Autowired
    private McpResourceRegistry resourceRegistry;
    
    @Test
    public void testServerInitialization() {
        Map<String, Object> info = mcpServer.getServerInfo();
        assertNotNull(info);
        assertEquals("AiTestProject-MCP-Server", info.get("name"));
        assertEquals("1.0.0", info.get("version"));
        assertEquals("2024-11-05", info.get("protocolVersion"));
    }
    
    @Test
    public void testToolsRegistered() {
        List<McpTool> tools = toolRegistry.listTools();
        assertNotNull(tools);
        assertTrue(tools.size() >= 2, "应至少注册2个工具");
        
        // 检查天气工具
        assertTrue(toolRegistry.hasTool("get_weather"), "应注册天气工具");
        
        // 检查股票工具
        assertTrue(toolRegistry.hasTool("get_stock"), "应注册股票工具");
    }
    
    @Test
    public void testResourcesRegistered() {
        List<McpResource> resources = resourceRegistry.listResources();
        assertNotNull(resources);
        assertTrue(resources.size() >= 1, "应至少注册1个资源");
    }
    
    @Test
    public void testToolsList() {
        McpRequest request = McpRequest.builder()
            .jsonrpc("2.0")
            .id("test-1")
            .method("tools/list")
            .params(Map.of())
            .build();
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("test-1", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testWeatherToolCall() throws Exception {
        Map<String, Object> args = Map.of("city", "北京");
        Object result = toolRegistry.executeTool("get_weather", args);
        
        assertNotNull(result);
        assertTrue(result instanceof Map, "天气工具应返回Map");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> weatherData = (Map<String, Object>) result;
        
        // 验证返回的数据包含必要字段（可能有error或weather数据）
        assertTrue(weatherData.containsKey("city") || weatherData.containsKey("error"),
            "应包含city或error字段");
    }
    
    @Test
    public void testStockToolCall() throws Exception {
        Map<String, Object> args = Map.of("ticker", "AAPL");
        Object result = toolRegistry.executeTool("get_stock", args);
        
        assertNotNull(result);
        assertTrue(result instanceof Map, "股票工具应返回Map");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stockData = (Map<String, Object>) result;
        
        assertTrue(stockData.containsKey("ticker"), "应包含ticker字段");
        assertTrue(stockData.containsKey("price"), "应包含price字段");
        assertTrue(stockData.containsKey("change"), "应包含change字段");
    }
    
    @Test
    public void testMcpProtocolToolCall() {
        McpRequest request = McpRequest.builder()
            .jsonrpc("2.0")
            .id("test-2")
            .method("tools/call")
            .params(Map.of(
                "name", "get_stock",
                "arguments", Map.of("ticker", "TSLA")
            ))
            .build();
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("test-2", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testResourcesList() {
        McpRequest request = McpRequest.builder()
            .jsonrpc("2.0")
            .id("test-3")
            .method("resources/list")
            .params(Map.of())
            .build();
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("test-3", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testPing() {
        McpRequest request = McpRequest.builder()
            .jsonrpc("2.0")
            .id("test-4")
            .method("ping")
            .params(Map.of())
            .build();
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("test-4", response.getId());
        assertNull(response.getError());
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) response.getResult();
        assertEquals("pong", result.get("status"));
    }
    
    @Test
    public void testUnknownMethod() {
        McpRequest request = McpRequest.builder()
            .jsonrpc("2.0")
            .id("test-5")
            .method("unknown/method")
            .params(Map.of())
            .build();
        
        McpResponse<?> response = mcpServer.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("test-5", response.getId());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
    }
}








