# MCP (Model Context Protocol) 架构说明

## 概述

本项目已升级为符合 **Model Context Protocol (MCP)** 标准的架构。MCP 是一个标准化协议，用于AI应用与外部工具和资源之间的通信。

## 架构特点

### 1. 标准化协议
- 符合 MCP 2024-11-05 版本规范
- 使用 JSON-RPC 2.0 作为传输协议
- 支持工具调用、资源访问等核心功能

### 2. 模块化设计
```
mcp/
├── protocol/          # MCP协议定义
│   ├── McpRequest     # 请求对象
│   ├── McpResponse    # 响应对象
│   └── McpError       # 错误对象
├── model/             # MCP数据模型
│   ├── McpTool        # 工具定义
│   ├── McpResource    # 资源定义
│   └── McpToolInputSchema # 工具参数Schema
├── core/              # 核心组件
│   ├── McpToolRegistry      # 工具注册中心
│   ├── McpResourceRegistry  # 资源注册中心
│   ├── McpToolExecutor      # 工具执行器接口
│   └── McpResourceProvider  # 资源提供者接口
├── server/            # MCP服务器
│   └── McpServer      # 协议处理服务器
├── tools/             # MCP工具实现
│   ├── McpWeatherTool # 天气查询工具
│   └── McpStockTool   # 股票查询工具
├── resources/         # MCP资源实现
│   ├── ConversationHistoryResource # 对话历史
│   └── SystemPromptResource        # 系统提示词
├── service/           # MCP服务
│   └── McpOllamaService # MCP集成的Ollama服务
├── controller/        # HTTP API
│   └── McpController  # MCP REST接口
└── config/            # 配置
    ├── McpConfiguration         # 工具配置
    └── McpResourceConfiguration # 资源配置
```

## 核心功能

### 1. 工具管理 (Tools)

#### 已注册工具

**天气查询工具 (get_weather)**
- 功能：查询指定城市的实时天气
- 参数：
  - `city` (必需): 城市名称
  - `date` (可选): 日期
- 返回：温度、天气描述、湿度、风力等详细信息

**股票查询工具 (get_stock)**
- 功能：查询股票实时价格
- 参数：
  - `ticker` (必需): 股票代码
- 返回：股票代码、价格、涨跌幅

#### 工具注册示例
```java
@Component
public class MyMcpTool implements McpToolExecutor {
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) {
        // 工具执行逻辑
        return result;
    }
    
    @Override
    public McpTool getToolDefinition() {
        return McpTool.builder()
            .name("my_tool")
            .description("工具描述")
            .inputSchema(schema)
            .build();
    }
}
```

### 2. 资源管理 (Resources)

#### 已注册资源

**对话历史 (conversation://history/{sessionId})**
- URI格式: `conversation://history/{sessionId}?limit=N`
- 功能：获取会话的对话历史记录
- 参数：
  - `sessionId`: 会话ID
  - `limit`: 消息数量（可选）

**系统提示词 (prompt://system/{type})**
- URI格式: `prompt://system/{type}`
- 支持类型：
  - `react`: ReAct提示词模板
  - `assistant`: 助手默认提示词

#### 资源注册示例
```java
@Component
public class MyResource implements McpResourceProvider {
    @Override
    public Object readResource(String uri) {
        // 资源读取逻辑
        return content;
    }
    
    @Override
    public McpResource getResourceDefinition() {
        return McpResource.builder()
            .uri("myresource://path/{id}")
            .name("资源名称")
            .description("资源描述")
            .mimeType("text/plain")
            .build();
    }
}
```

## MCP 协议使用

### HTTP API

**基础地址**: `http://localhost:8080/api/mcp`

### 1. 初始化
```json
POST /api/mcp
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "initialize",
  "params": {}
}
```

**响应**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "name": "AiTestProject-MCP-Server",
    "version": "1.0.0",
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {"listChanged": true},
      "resources": {"subscribe": true, "listChanged": true}
    }
  }
}
```

### 2. 列出工具
```json
POST /api/mcp
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/list",
  "params": {}
}
```

**响应**:
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "tools": [
      {
        "name": "get_weather",
        "description": "获取指定城市的实时天气数据...",
        "inputSchema": {
          "type": "object",
          "properties": {
            "city": {"type": "string", "description": "城市名称"},
            "date": {"type": "string", "description": "日期（可选）"}
          },
          "required": ["city"]
        }
      }
    ]
  }
}
```

### 3. 调用工具
```json
POST /api/mcp
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "get_weather",
    "arguments": {
      "city": "北京"
    }
  }
}
```

**响应**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\n  \"city\": \"北京\",\n  \"temp\": 15.5,\n  \"text\": \"晴\",\n  ...\n}"
      }
    ],
    "isError": false
  }
}
```

### 4. 列出资源
```json
POST /api/mcp
{
  "jsonrpc": "2.0",
  "id": "4",
  "method": "resources/list",
  "params": {}
}
```

### 5. 读取资源
```json
POST /api/mcp
{
  "jsonrpc": "2.0",
  "id": "5",
  "method": "resources/read",
  "params": {
    "uri": "conversation://history/session123?limit=10"
  }
}
```

## 集成到应用

### 使用 McpOllamaService

```java
@Autowired
private McpOllamaService mcpOllamaService;

// 生成响应（自动处理工具调用）
String response = mcpOllamaService.generateResponse(sessionId, userMessage);

// 获取可用工具
List<McpTool> tools = mcpOllamaService.getAvailableTools();

// 获取对话历史
List<ChatMessage> history = mcpOllamaService.getConversationHistory(sessionId, 20);
```

### 直接使用 McpServer

```java
@Autowired
private McpServer mcpServer;

// 处理MCP请求
McpRequest request = McpRequest.builder()
    .method("tools/call")
    .params(Map.of("name", "get_weather", "arguments", Map.of("city", "上海")))
    .id("req-1")
    .build();

McpResponse<?> response = mcpServer.handleRequest(request);
```

## 扩展开发

### 添加新工具

1. 创建工具类实现 `McpToolExecutor`
2. 在 `McpConfiguration` 中注册
3. 工具自动加入MCP协议

### 添加新资源

1. 创建资源类实现 `McpResourceProvider`
2. 在 `McpResourceConfiguration` 中注册
3. 资源自动加入MCP协议

## 优势

1. **标准化**: 符合MCP协议标准，易于与其他MCP兼容系统集成
2. **可扩展**: 工具和资源通过注册机制轻松扩展
3. **解耦合**: 工具实现与协议分离，便于维护和测试
4. **类型安全**: 使用JSON Schema定义参数，支持验证
5. **可观测**: 完整的日志记录和错误处理

## 测试

### 使用curl测试

```bash
# 获取服务器信息
curl http://localhost:8080/api/mcp/info

# 健康检查
curl http://localhost:8080/api/mcp/health

# 列出工具
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'

# 调用天气工具
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "get_weather",
      "arguments": {"city": "北京"}
    }
  }'
```

## 参考资料

- [Model Context Protocol 官方文档](https://modelcontextprotocol.io)
- [MCP Specification](https://spec.modelcontextprotocol.io)
- [JSON-RPC 2.0 规范](https://www.jsonrpc.org/specification)

## 版本历史

- **v1.0.0** (2025-10-29): 初始MCP架构实现
  - 核心协议实现
  - 工具和资源管理
  - HTTP API接口
  - 天气和股票工具
  - 对话历史和提示词资源








