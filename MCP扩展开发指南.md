# MCP 扩展开发指南

> 如何在MCP架构中扩展工具和调整提示词

---

## 🔧 一、工具扩展

### 📍 核心位置一览

| 位置 | 文件路径 | 作用 |
|------|---------|------|
| **工具定义** | `src/main/java/com/cuixing/aitestproject/mcp/tools/` | 创建新工具类 |
| **工具注册** | `src/main/java/com/cuixing/aitestproject/mcp/config/McpConfiguration.java` | 注册工具到系统 |
| **工具执行** | 自动处理 | MCP框架自动调用 |

### 🎯 步骤1: 创建工具类

**位置**: `src/main/java/com/cuixing/aitestproject/mcp/tools/`

**模板代码**:
```java
package com.cuixing.aitestproject.mcp.tools;

import com.cuixing.aitestproject.mcp.core.McpToolExecutor;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema.PropertySchema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Mcp你的工具名Tool implements McpToolExecutor {
    
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
        // 1️⃣ 获取参数
        String param1 = (String) arguments.get("param1");
        int param2 = (int) arguments.get("param2");
        
        // 2️⃣ 执行业务逻辑
        String result = doSomething(param1, param2);
        
        // 3️⃣ 返回结果（Map格式）
        return Map.of(
            "success", true,
            "result", result,
            "message", "执行成功"
        );
    }
    
    @Override
    public McpTool getToolDefinition() {
        // 4️⃣ 定义工具的输入参数
        McpToolInputSchema inputSchema = McpToolInputSchema.builder()
            .type("object")
            .properties(Map.of(
                "param1", PropertySchema.builder()
                    .type("string")
                    .description("参数1的描述")
                    .build(),
                "param2", PropertySchema.builder()
                    .type("number")
                    .description("参数2的描述")
                    .build()
            ))
            .required(List.of("param1", "param2"))  // 必需参数
            .build();
        
        // 5️⃣ 定义工具信息
        return McpTool.builder()
            .name("your_tool_name")  // 工具名称（唯一）
            .description("工具的详细描述，AI会根据这个来决定何时调用")
            .inputSchema(inputSchema)
            .metadata(Map.of(
                "category", "工具分类",
                "version", "1.0"
            ))
            .build();
    }
    
    // 你的业务逻辑
    private String doSomething(String param1, int param2) {
        // 实现你的功能
        return "result";
    }
}
```

### 🎯 步骤2: 注册工具

**位置**: `src/main/java/com/cuixing/aitestproject/mcp/config/McpConfiguration.java`

**修改点**:

```java
@Configuration
public class McpConfiguration {
    
    private final McpToolRegistry toolRegistry;
    private final McpWeatherTool weatherTool;
    private final McpStockTool stockTool;
    private final Mcp你的工具名Tool 你的工具变量;  // ✅ 1. 添加字段
    
    @Autowired
    public McpConfiguration(
            McpToolRegistry toolRegistry,
            McpWeatherTool weatherTool,
            McpStockTool stockTool,
            Mcp你的工具名Tool 你的工具变量) {  // ✅ 2. 添加构造参数
        this.toolRegistry = toolRegistry;
        this.weatherTool = weatherTool;
        this.stockTool = stockTool;
        this.你的工具变量 = 你的工具变量;  // ✅ 3. 赋值
    }
    
    @PostConstruct
    public void registerTools() {
        // ... 其他工具注册
        
        // ✅ 4. 注册你的工具
        toolRegistry.registerTool(
            你的工具变量.getToolDefinition(),
            你的工具变量
        );
    }
}
```

### 📝 实战示例：计算器工具

已创建文件：
- ✅ `src/main/java/com/cuixing/aitestproject/mcp/tools/McpCalculatorTool.java`
- ✅ 已在 `McpConfiguration.java` 中注册

**功能**：支持加减乘除四则运算

**使用方式**：
```
用户：10加5等于多少？
AI会自动调用：[TOOL:calculator] {"a":10, "b":5, "operation":"add"}
AI返回：10 + 5 = 15
```

---

## 💬 二、提示词调整

### 📍 核心位置一览

| 位置 | 文件路径 | 作用 |
|------|---------|------|
| **提示词配置** | `src/main/java/com/cuixing/aitestproject/mcp/config/McpPromptConfig.java` | 集中管理所有提示词 ⭐推荐 |
| **服务集成** | `src/main/java/com/cuixing/aitestproject/mcp/service/McpOllamaService.java` | 使用提示词 |
| **资源提供** | `src/main/java/com/cuixing/aitestproject/mcp/resources/SystemPromptResource.java` | 提示词资源 |

### 🎯 方式1: 修改集中配置（推荐）⭐

**位置**: `src/main/java/com/cuixing/aitestproject/mcp/config/McpPromptConfig.java`

已创建完整的提示词配置文件，包含以下可调整部分：

#### 1️⃣ 系统提示词
```java
public String getSystemPrompt() {
    return """
        你是一个专业的AI智能助手，具有以下能力和特点：
        
        【核心能力】
        1. 工具调用：可以使用多种工具来获取实时信息
        2. 上下文记忆：能够记住并理解对话历史
        3. 智能推理：根据上下文推断用户意图
        
        【行为准则】
        1. 准确性：提供准确、可靠的信息
        2. 友好性：使用友好、专业的语气
        3. 主动性：主动使用工具获取信息，而不是猜测
        4. 连贯性：基于对话历史保持上下文连贯
        
        // 💬 在这里修改AI的角色定位、行为准则等
        """;
}
```

**调整建议**：
- 修改角色定位（如：专业客服、技术顾问、教育导师）
- 添加特定领域知识
- 定义回答风格（正式/友好/简洁）
- 设置回答格式要求

#### 2️⃣ 工具使用提示
```java
public String getToolUsagePrompt() {
    return """
        【工具使用说明】
        格式：[TOOL:工具名] {参数JSON}
        
        示例：
        - 查天气：[TOOL:get_weather] {"city": "北京"}
        - 查股票：[TOOL:get_stock] {"ticker": "AAPL"}
        
        // 💬 在这里添加新工具的使用说明
        """;
}
```

#### 3️⃣ 上下文理解提示
```java
public String getContextPrompt() {
    return """
        【上下文理解规则】
        请务必：
        1. 仔细阅读历史上下文
        2. 理解用户的后续问题与历史的关联
        3. 基于历史信息给出连贯的回答
        
        // 💬 在这里调整如何处理历史对话
        """;
}
```

#### 4️⃣ 领域专业提示
```java
public String getDomainPrompt(String domain) {
    return switch (domain) {
        case "weather" -> """
            【天气领域专业知识】
            - 温度范围建议
            - 天气活动建议
            - 穿衣建议
            """;
        
        case "finance" -> """
            【金融领域专业提示】
            - 免责声明
            - 风险提示
            """;
        
        // 💬 添加新的领域提示
        case "你的领域" -> """
            【你的领域专业知识】
            """;
        
        default -> "";
    };
}
```

### 🎯 方式2: 直接修改服务代码

**位置**: `src/main/java/com/cuixing/aitestproject/mcp/service/McpOllamaService.java`

**第98行**：
```java
// 💬 系统指令 - 使用配置的提示词
prompt.append(promptConfig.getSystemPrompt()).append("\n\n");
```

如果需要临时修改或调试，可以直接在这里修改：
```java
prompt.append("""
    你的自定义提示词...
    """).append("\n\n");
```

### 🎯 方式3: 修改资源提示词

**位置**: `src/main/java/com/cuixing/aitestproject/mcp/resources/SystemPromptResource.java`

**第48行** - `getAssistantPrompt()` 方法：
```java
private String getAssistantPrompt() {
    return """
        你是一个智能助手，可以帮助用户查询天气、股票等信息。
        
        // 💬 修改通过资源访问的提示词
        """;
}
```

---

## 🎨 三、提示词调整场景示例

### 场景1: 专业客服助手

```java
public String getSystemPrompt() {
    return """
        你是一个专业的客服助手，为用户提供友好、高效的服务。
        
        【服务理念】
        1. 客户至上：始终以客户需求为中心
        2. 耐心专业：用专业知识耐心解答每一个问题
        3. 主动服务：主动发现并解决潜在问题
        
        【沟通风格】
        - 称呼：使用"您"
        - 语气：温和、友好、专业
        - 回答：先道歉（如果适用），再解决问题，最后确认
        
        【处理流程】
        1. 理解问题
        2. 使用工具获取信息
        3. 清晰解答
        4. 询问是否还有其他需要帮助的
        """;
}
```

### 场景2: 技术顾问

```java
public String getSystemPrompt() {
    return """
        你是一个资深技术顾问，提供专业的技术咨询服务。
        
        【专业能力】
        1. 技术深度：准确理解技术问题本质
        2. 实战经验：提供可行的解决方案
        3. 持续学习：关注最新技术趋势
        
        【回答风格】
        - 结构化：问题分析 -> 解决方案 -> 最佳实践
        - 代码示例：必要时提供示例代码
        - 注意事项：强调潜在风险和注意事项
        
        【服务范围】
        - 技术咨询
        - 方案设计
        - 问题诊断
        - 性能优化
        """;
}
```

### 场景3: 教育导师

```java
public String getSystemPrompt() {
    return """
        你是一个耐心的教育导师，帮助学生理解和掌握知识。
        
        【教学理念】
        1. 因材施教：根据学生水平调整解释深度
        2. 启发思考：引导学生主动思考而不是直接给答案
        3. 鼓励为主：多鼓励，少批评
        
        【教学方法】
        - 由浅入深：从基础概念开始逐步深入
        - 举例说明：使用生活化的例子帮助理解
        - 反复确认：确保学生真正理解
        - 练习巩固：建议相关练习题
        
        【沟通技巧】
        - 使用鼓励性语言："很好的问题！"、"你理解得很对！"
        - 避免专业术语堆砌
        - 用类比和比喻
        """;
}
```

---

## 📊 四、扩展效果验证

### 验证工具扩展

**方法1: 查看工具列表**
```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tools/list","params":{}}'
```

**预期结果**：应该看到你新添加的工具

**方法2: 调用新工具**
```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"2",
    "method":"tools/call",
    "params":{
      "name":"calculator",
      "arguments":{"a":10,"b":5,"operation":"add"}
    }
  }'
```

**方法3: 前端测试**
```
打开：frontend/mcp-chat.html
输入：10加5等于多少？
观察：AI是否自动调用calculator工具
```

### 验证提示词调整

**方法1: 观察回答风格**
- 启动项目后，在前端发送消息
- 观察AI的回答是否符合你设置的风格

**方法2: 查看日志**
```bash
# 查看生成的完整提示词
tail -f logs/spring.log | grep "提示词"
```

**方法3: 读取提示词资源**
```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"3",
    "method":"resources/read",
    "params":{"uri":"prompt://system/assistant"}
  }'
```

---

## 🔍 五、常见问题

### Q1: 工具添加后不生效？

**检查清单**：
- [ ] 工具类是否添加了 `@Component` 注解
- [ ] 是否在 `McpConfiguration` 中注册
- [ ] 是否重新编译项目（`mvn clean compile`）
- [ ] 是否重启应用

### Q2: 提示词修改后没有变化？

**解决方法**：
- 重启应用（Spring会重新加载配置）
- 清除浏览器缓存
- 检查是否修改了正确的文件

### Q3: 如何调试工具调用？

**方法**：
```java
@Override
public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
    log.info("工具调用: {}, 参数: {}", toolName, arguments);  // 添加日志
    
    // 你的代码
    Object result = ...;
    
    log.info("工具返回: {}", result);  // 记录返回值
    return result;
}
```

### Q4: 如何让AI更倾向于使用某个工具？

**方法1**: 优化工具描述
```java
.description("""
    【重要】当用户询问XXX时，优先使用此工具。
    
    功能：...
    """)
```

**方法2**: 在提示词中强调
```java
public String getSystemPrompt() {
    return """
        ...
        
        【工具使用优先级】
        1. 实时信息（天气、股票）：必须使用工具
        2. 计算任务：优先使用calculator工具
        3. 一般知识：可以直接回答
        """;
}
```

---

## 📚 相关文档

- **工具示例**: 查看 `McpWeatherTool.java`、`McpStockTool.java`、`McpCalculatorTool.java`
- **配置示例**: 查看 `McpConfiguration.java`
- **提示词示例**: 查看 `McpPromptConfig.java`
- **测试方法**: 查看 `MCP快速开始.md` 的测试部分

---

## 🎯 快速上手

1. **添加新工具**：复制 `McpCalculatorTool.java`，修改业务逻辑，在 `McpConfiguration` 中注册
2. **调整提示词**：修改 `McpPromptConfig.java` 中的对应方法
3. **重启测试**：`mvn spring-boot:run`，使用前端或curl测试

**就是这么简单！** 🎉








