# MCP 快速开始指南

## 前置条件

1. Java 17+
2. Maven 3.6+
3. Ollama (运行在 http://localhost:11434)
4. MySQL 数据库

## 启动步骤

### 1. 启动后端服务

```bash
# 编译项目
mvn clean package -DskipTests

# 运行
java -jar target/AiTestProject-0.0.1-SNAPSHOT.jar
```

或使用Maven插件：
```bash
mvn spring-boot:run
```

### 2. 验证服务

访问健康检查接口：
```bash
curl http://localhost:8080/api/mcp/health
```

预期响应：
```json
{
  "status": "healthy",
  "protocol": "MCP",
  "version": "1.0.0"
}
```

### 3. 启动前端（可选）

```bash
# Windows
start-frontend.bat

# Linux/Mac
./start-frontend.sh
```

访问：http://localhost:3000

## 快速测试

### 测试1: 列出所有工具

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'
```

### 测试2: 调用天气工具

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "get_weather",
      "arguments": {
        "city": "北京"
      }
    }
  }'
```

### 测试3: 调用股票工具

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "get_stock",
      "arguments": {
        "ticker": "AAPL"
      }
    }
  }'
```

### 测试4: 列出资源

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "4",
    "method": "resources/list",
    "params": {}
  }'
```

### 测试5: 读取对话历史

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "5",
    "method": "resources/read",
    "params": {
      "uri": "conversation://history/test-session?limit=5"
    }
  }'
```

## 在代码中使用

### 方式1: 使用 McpOllamaService (推荐)

```java
@Autowired
private McpOllamaService mcpOllamaService;

public void chat() {
    String sessionId = "user-123";
    String userMessage = "北京天气怎么样？";
    
    // 自动处理工具调用
    String response = mcpOllamaService.generateResponse(sessionId, userMessage);
    
    System.out.println(response);
}
```

### 方式2: 直接调用 MCP Server

```java
@Autowired
private McpServer mcpServer;

public void callTool() {
    McpRequest request = McpRequest.builder()
        .jsonrpc("2.0")
        .id("req-1")
        .method("tools/call")
        .params(Map.of(
            "name", "get_weather",
            "arguments", Map.of("city", "上海")
        ))
        .build();
    
    McpResponse<?> response = mcpServer.handleRequest(request);
    System.out.println(response.getResult());
}
```

### 方式3: 直接使用工具注册中心

```java
@Autowired
private McpToolRegistry toolRegistry;

public void useToolRegistry() {
    // 列出所有工具
    List<McpTool> tools = toolRegistry.listTools();
    
    // 执行工具
    Map<String, Object> args = Map.of("city", "深圳");
    Object result = toolRegistry.executeTool("get_weather", args);
    
    System.out.println(result);
}
```

## WebSocket 聊天测试

打开浏览器访问：
- `frontend/index.html` - 主聊天界面
- `frontend/test-websocket.html` - WebSocket测试页面

在聊天框输入：
```
北京天气怎么样？
```

AI会自动调用天气工具并返回结果。

## 添加自定义工具

### 1. 创建工具类

```java
package com.cuixing.aitestproject.mcp.tools;

import com.cuixing.aitestproject.mcp.core.McpToolExecutor;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MyCustomTool implements McpToolExecutor {
    
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
        String param = (String) arguments.get("param");
        
        // 实现工具逻辑
        return Map.of("result", "处理结果: " + param);
    }
    
    @Override
    public McpTool getToolDefinition() {
        return McpTool.builder()
            .name("my_custom_tool")
            .description("自定义工具描述")
            .inputSchema(McpToolInputSchema.builder()
                .type("object")
                .properties(Map.of(
                    "param", PropertySchema.builder()
                        .type("string")
                        .description("参数描述")
                        .build()
                ))
                .required(List.of("param"))
                .build())
            .build();
    }
}
```

### 2. 注册工具

在 `McpConfiguration.java` 中添加：

```java
@Autowired
private MyCustomTool myCustomTool;

@PostConstruct
public void registerTools() {
    // ... 现有工具注册
    
    // 注册自定义工具
    toolRegistry.registerTool(
        myCustomTool.getToolDefinition(),
        myCustomTool
    );
}
```

### 3. 使用工具

```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "my_custom_tool",
      "arguments": {"param": "测试"}
    }
  }'
```

## 常见问题

### Q1: 端口被占用
修改 `application.properties`:
```properties
server.port=8081
```

### Q2: Ollama连接失败
确保Ollama服务运行：
```bash
ollama serve
```

检查模型是否下载：
```bash
ollama list
ollama pull qwen3:8b
```

### Q3: 数据库连接失败
检查 `application.properties` 中的数据库配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_chat_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Q4: 工具调用失败
查看日志：
```bash
tail -f logs/spring.log
```

检查工具是否注册：
```bash
curl http://localhost:8080/api/mcp/info
```

## 下一步

- 阅读 [MCP架构说明.md](MCP架构说明.md) 了解详细架构
- 查看示例代码了解更多用法
- 扩展自己的工具和资源

## 技术支持

如有问题，请查看：
1. 日志文件: `logs/spring.log`
2. 控制台输出
3. 项目文档








