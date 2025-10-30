# AiTestProject - MCP 架构版本

> 基于 Model Context Protocol (MCP) 标准的智能对话系统

## 🎯 项目概述

本项目是一个完整的 MCP（Model Context Protocol）实现，集成了 Ollama、LangChain4j、RAG 向量检索等技术，提供标准化的工具调用和资源管理能力。

### 核心特性

- ✅ **标准化 MCP 协议** - 符合 MCP 2024-11-05 规范
- ✅ **工具管理系统** - 灵活的工具注册和调用机制
- ✅ **资源管理系统** - 统一的资源访问接口
- ✅ **RAG 增强对话** - 基于向量数据库的上下文检索
- ✅ **WebSocket 实时通信** - 支持实时对话交互
- ✅ **RESTful API** - 完整的 HTTP 接口
- ✅ **可扩展架构** - 易于添加新工具和资源

## 📁 项目结构

```
AiTestProject/
├── src/main/java/com/cuixing/aitestproject/
│   ├── mcp/                          # MCP 核心模块
│   │   ├── protocol/                 # 协议定义
│   │   │   ├── McpRequest.java       # MCP 请求
│   │   │   ├── McpResponse.java      # MCP 响应
│   │   │   └── McpError.java         # 错误对象
│   │   ├── model/                    # 数据模型
│   │   │   ├── McpTool.java          # 工具定义
│   │   │   ├── McpResource.java      # 资源定义
│   │   │   └── McpToolInputSchema.java # 参数 Schema
│   │   ├── core/                     # 核心组件
│   │   │   ├── McpToolRegistry.java  # 工具注册中心
│   │   │   ├── McpResourceRegistry.java # 资源注册中心
│   │   │   ├── McpToolExecutor.java  # 工具执行器
│   │   │   └── McpResourceProvider.java # 资源提供者
│   │   ├── server/                   # MCP 服务器
│   │   │   └── McpServer.java        # 协议处理服务器
│   │   ├── tools/                    # 工具实现
│   │   │   ├── McpWeatherTool.java   # 天气工具
│   │   │   └── McpStockTool.java     # 股票工具
│   │   ├── resources/                # 资源实现
│   │   │   ├── ConversationHistoryResource.java # 对话历史
│   │   │   └── SystemPromptResource.java # 系统提示词
│   │   ├── service/                  # MCP 服务
│   │   │   └── McpOllamaService.java # MCP Ollama 集成
│   │   ├── controller/               # HTTP 控制器
│   │   │   └── McpController.java    # REST API
│   │   └── config/                   # 配置类
│   │       ├── McpConfiguration.java
│   │       ├── McpResourceConfiguration.java
│   │       └── McpWebSocketConfig.java
│   ├── chat/                         # WebSocket 处理
│   │   ├── ChatWebSocketHandler.java
│   │   └── McpChatWebSocketHandler.java # MCP WebSocket
│   ├── entity/                       # 实体类
│   ├── repository/                   # 数据访问层
│   └── service/                      # 业务服务
│       └── RagService.java           # RAG 服务
├── frontend/                         # 前端页面
│   ├── index.html                    # 原版聊天界面
│   └── mcp-chat.html                 # MCP 聊天界面
├── test-mcp-api.sh                   # Linux/Mac 测试脚本
├── test-mcp-api.bat                  # Windows 测试脚本
├── MCP架构说明.md                    # 详细架构文档
├── MCP快速开始.md                    # 快速入门指南
└── README-MCP.md                     # 本文件
```

## 🚀 快速开始

### 前置要求

1. **Java 17+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Ollama** (运行在 http://localhost:11434)
   ```bash
   # 安装 Ollama
   curl -fsSL https://ollama.com/install.sh | sh
   
   # 下载模型
   ollama pull qwen3:8b
   ```

### 启动步骤

1. **配置数据库**
   ```sql
   CREATE DATABASE ai_chat_db;
   ```

2. **更新配置** (`src/main/resources/application.properties`)
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ai_chat_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **编译运行**
   ```bash
   # 编译
   mvn clean package -DskipTests
   
   # 运行
   java -jar target/AiTestProject-0.0.1-SNAPSHOT.jar
   ```

4. **访问应用**
   - MCP API: http://localhost:8080/api/mcp
   - 健康检查: http://localhost:8080/api/mcp/health
   - MCP 聊天界面: file:///.../frontend/mcp-chat.html

## 🧪 测试

### 使用测试脚本

**Linux/Mac:**
```bash
./test-mcp-api.sh
```

**Windows:**
```bash
test-mcp-api.bat
```

### 手动测试

**1. 健康检查**
```bash
curl http://localhost:8080/api/mcp/health
```

**2. 列出工具**
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

**3. 调用天气工具**
```bash
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

## 🛠️ 开发指南

### 添加自定义工具

1. **创建工具类**
```java
@Component
public class MyTool implements McpToolExecutor {
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) {
        // 实现工具逻辑
        return result;
    }
    
    @Override
    public McpTool getToolDefinition() {
        return McpTool.builder()
            .name("my_tool")
            .description("工具描述")
            .inputSchema(...)
            .build();
    }
}
```

2. **注册工具** (在 `McpConfiguration.java`)
```java
@Autowired
private MyTool myTool;

@PostConstruct
public void registerTools() {
    toolRegistry.registerTool(myTool.getToolDefinition(), myTool);
}
```

### 添加自定义资源

1. **创建资源类**
```java
@Component
public class MyResource implements McpResourceProvider {
    @Override
    public Object readResource(String uri) {
        // 实现资源读取逻辑
        return content;
    }
    
    @Override
    public McpResource getResourceDefinition() {
        return McpResource.builder()
            .uri("myresource://...")
            .name("资源名称")
            .description("资源描述")
            .build();
    }
}
```

2. **注册资源** (在 `McpResourceConfiguration.java`)
```java
@Autowired
private MyResource myResource;

@PostConstruct
public void registerResources() {
    resourceRegistry.registerResource(
        myResource.getResourceDefinition(), 
        myResource
    );
}
```

## 📚 API 文档

### MCP 协议方法

| 方法 | 说明 | 参数 |
|------|------|------|
| `initialize` | 初始化连接 | 无 |
| `ping` | 健康检查 | 无 |
| `tools/list` | 列出所有工具 | 无 |
| `tools/call` | 调用工具 | `name`, `arguments` |
| `resources/list` | 列出所有资源 | 无 |
| `resources/read` | 读取资源 | `uri` |

### 已注册工具

| 工具名 | 说明 | 参数 |
|--------|------|------|
| `get_weather` | 查询天气 | `city` (必需), `date` (可选) |
| `get_stock` | 查询股票 | `ticker` (必需) |

### 已注册资源

| URI | 说明 |
|-----|------|
| `conversation://history/{sessionId}?limit=N` | 对话历史 |
| `prompt://system/react` | ReAct 提示词 |
| `prompt://system/assistant` | 助手提示词 |

## 🌟 技术栈

- **Java 17** - 编程语言
- **Spring Boot 3.2** - 应用框架
- **LangChain4j** - AI 应用框架
- **Ollama** - 本地 LLM 运行时
- **MySQL** - 关系数据库
- **JPA/Hibernate** - ORM 框架
- **WebSocket** - 实时通信
- **Jackson** - JSON 处理
- **Maven** - 项目管理

## 📖 相关文档

- [MCP架构说明.md](MCP架构说明.md) - 详细的架构设计文档
- [MCP快速开始.md](MCP快速开始.md) - 快速入门和测试指南
- [Model Context Protocol 官方文档](https://modelcontextprotocol.io)

## 🔧 配置说明

### application.properties

```properties
# 服务器端口
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/ai_chat_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Ollama 配置（在代码中配置）
# baseUrl=http://localhost:11434
# modelName=qwen3:8b
```

## 🐛 故障排除

### 问题1: 端口占用
**解决**: 修改 `application.properties` 中的 `server.port`

### 问题2: Ollama 连接失败
**解决**: 
```bash
# 启动 Ollama
ollama serve

# 检查模型
ollama list
ollama pull qwen3:8b
```

### 问题3: 数据库连接失败
**解决**: 检查数据库配置和连接参数

### 问题4: 工具调用失败
**解决**: 
1. 查看日志: `tail -f logs/spring.log`
2. 检查工具注册: `curl http://localhost:8080/api/mcp/info`

## 🎓 示例用法

### Java 代码示例

```java
// 使用 McpOllamaService
@Autowired
private McpOllamaService mcpService;

public void chat() {
    String response = mcpService.generateResponse(
        "session-123", 
        "北京天气怎么样？"
    );
    System.out.println(response);
}

// 直接使用工具注册中心
@Autowired
private McpToolRegistry toolRegistry;

public void callTool() {
    Object result = toolRegistry.executeTool(
        "get_weather",
        Map.of("city", "上海")
    );
}
```

### cURL 示例

```bash
# 调用天气工具
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "get_weather",
      "arguments": {"city": "深圳"}
    }
  }'
```

## 📊 性能优化

- **工具注册**: 使用 ConcurrentHashMap 支持并发访问
- **资源缓存**: 可以为常用资源添加缓存层
- **连接池**: HikariCP 数据库连接池
- **向量检索**: 基于余弦相似度的高效检索

## 🔐 安全考虑

- **输入验证**: 所有工具参数都应验证
- **权限控制**: 可以添加基于角色的访问控制
- **日志审计**: 完整的操作日志记录
- **错误处理**: 统一的异常处理机制

## 🚀 未来计划

- [ ] 添加更多内置工具（计算器、翻译等）
- [ ] 支持流式响应
- [ ] 添加工具调用链
- [ ] 实现资源订阅机制
- [ ] 添加监控和指标
- [ ] 支持多租户

## 📝 License

MIT License

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题，请查看文档或提交 Issue。

---

**Happy Coding! 🎉**








