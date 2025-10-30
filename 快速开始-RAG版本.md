# 快速开始 - RAG 向量数据库版本 🚀

## ✅ 前置要求

### 1. MySQL 数据库
```bash
# 确保 MySQL 8.0+ 运行中
mysql -u root -p

# 输入密码: root
# 数据库会自动创建，无需手动创建
```

### 2. Ollama 服务
```bash
# 启动 Ollama
ollama serve

# 确认模型已下载
ollama list
# 如果没有 qwen3:8b，下载它
ollama pull qwen3:8b
```

### 3. Maven
```bash
# 确认 Maven 已安装
mvn -version
```

## 🚀 启动步骤

### 1. 启动后端服务
```bash
cd d:\cuixing-project\AiTestProject
mvn clean install
mvn spring-boot:run
```

**期待的日志输出**：
```
Hibernate: create table chat_sessions ...
Hibernate: create table chat_messages ...
Hibernate: create table vector_embeddings ...
RAG Service 初始化完成，嵌入模型已加载
OllamaService 初始化完成 - 使用 RAG 增强
Started AiTestProjectApplication in X seconds
```

### 2. 启动前端服务
```bash
# Windows
cd frontend
start-frontend.bat

# 或者手动
node server.js
```

### 3. 打开浏览器
```
http://localhost:3000
```

## 🎯 测试 RAG 功能

### 测试 1：基本对话 + 向量存储
```
1. 输入: "鹰潭的天气怎么样？"
   → 后端: 保存消息到 MySQL
   → 后端: 生成384维向量
   → 后端: 保存向量到数据库
   → AI: 调用天气工具
   → 后端: 保存工具结果和向量
   → AI: 返回天气信息

2. 检查数据库:
   mysql -u root -p
   USE ai_chat_db;
   SELECT COUNT(*) FROM chat_messages;  -- 应该有2-3条
   SELECT COUNT(*) FROM vector_embeddings;  -- 应该有2-3条
```

### 测试 2：RAG 检索（关键）
```
1. 输入: "适合什么活动？"
   → 后端: 生成查询向量
   → 后端: 从数据库检索相似向量
   → 后端: 找到之前的天气信息
   → 后端: 构建增强提示词
   → AI: 基于历史天气信息回答 ✅
   → 预期: "根据鹰潭今天25°C的晴朗天气，适合..."

2. 查看检索日志:
   检索相关消息: 找到 X 条
   相似度分数: 0.85, 0.76, ...
```

### 测试 3：多轮对话
```
1. 鹰潭的天气怎么样？
2. 上海呢？
3. 哪个城市更适合户外活动？
   → AI 应该对比两个城市的天气 ✅
```

### 测试 4：清除对话
```
1. 点击"清除对话"按钮
2. 确认清除
3. 再次询问: "刚才查询的是哪个城市？"
   → AI: 不知道（数据已清除）✅

4. 检查数据库:
   SELECT * FROM chat_sessions WHERE session_id = 'xxx';
   -- status 应该是 'closed'
   SELECT COUNT(*) FROM chat_messages WHERE ...;
   -- 应该是 0（已删除）
```

## 🔍 验证 RAG 工作

### 查看数据库数据

#### 1. 查看会话
```sql
SELECT * FROM chat_sessions;
```

期待输出：
```
id | session_id | created_at | last_active_at | status
1  | abc-123    | 2025-10-29 | 2025-10-29     | active
```

#### 2. 查看消息
```sql
SELECT 
    id,
    message_type,
    LEFT(content, 50) as content_preview,
    timestamp
FROM chat_messages
ORDER BY timestamp DESC
LIMIT 10;
```

期待输出：
```
id | message_type | content_preview                     | timestamp
5  | assistant    | 根据鹰潭今天25°C的晴朗天气...      | 2025-10-29 10:30
4  | user         | 适合什么活动？                      | 2025-10-29 10:30
3  | tool_result  | 工具调用：get_weather，参数：...   | 2025-10-29 10:29
2  | assistant    | 鹰潭今天天气晴朗...                | 2025-10-29 10:29
1  | user         | 鹰潭的天气怎么样？                  | 2025-10-29 10:29
```

#### 3. 查看向量
```sql
SELECT 
    m.message_type,
    LEFT(m.content, 30) as content,
    v.dimension,
    LENGTH(v.vector_data) as vector_length
FROM vector_embeddings v
JOIN chat_messages m ON m.id = v.message_id
ORDER BY v.created_at DESC
LIMIT 5;
```

期待输出：
```
message_type | content            | dimension | vector_length
assistant    | 根据鹰潭今天...    | 384       | 4567
user         | 适合什么活动？     | 384       | 4562
tool_result  | 工具调用：...      | 384       | 4590
```

### 查看控制台日志

```
为会话 xxx 创建新的 AI Assistant (RAG 模式)
保存用户消息: 鹰潭的天气怎么样？
生成向量嵌入成功: 384 维
检索相关消息: 找到 0 条（第一次）
调用 get_weather 工具...
保存工具调用结果
生成向量嵌入成功: 384 维
保存 AI 回复
---
保存用户消息: 适合什么活动？
生成向量嵌入成功: 384 维
检索相关消息: 找到 3 条 ✅
构建增强提示词（包含历史上下文）
调用 AI 生成回复
保存 AI 回复
```

## 🐛 故障排查

### 问题 1：数据库连接失败
```
错误: Communications link failure
```

**解决**：
```bash
# 检查 MySQL 是否运行
mysql -u root -p

# 确认密码是 root
# 确认 MySQL 监听 3306 端口
netstat -an | findstr 3306
```

### 问题 2：Ollama 连接失败
```
错误: Failed to connect to localhost:11434
```

**解决**：
```bash
# 启动 Ollama
ollama serve

# 测试连接
curl http://localhost:11434/api/tags
```

### 问题 3：嵌入模型加载失败
```
错误: Failed to load embedding model
```

**解决**：
- 嵌入模型会自动下载（约100MB）
- 确保网络连接正常
- 首次启动可能需要几分钟

### 问题 4：向量检索无结果
```
检索相关消息: 找到 0 条
```

**检查**：
```sql
-- 确认向量已保存
SELECT COUNT(*) FROM vector_embeddings;

-- 查看向量内容
SELECT * FROM vector_embeddings LIMIT 1;
```

**可能原因**：
- 首次对话时没有历史
- 会话ID不匹配
- 向量生成失败（查看日志）

## 📊 性能基准

### 首次启动
```
嵌入模型加载: ~5-10秒
数据库初始化: ~2-3秒
总启动时间: ~10-15秒
```

### 对话响应时间
```
保存消息: ~10-50ms
生成向量: ~50-200ms
检索向量: ~50-300ms (取决于数据量)
AI生成: ~2-5秒 (取决于模型)
总响应: ~3-6秒
```

### 数据库大小估算
```
每条消息: ~1KB (chat_messages)
每个向量: ~2KB (vector_embeddings)
100轮对话: ~300KB
1000轮对话: ~3MB
```

## 🎯 功能验证清单

- [ ] MySQL 连接成功
- [ ] 数据库自动创建 (ai_chat_db)
- [ ] 表自动创建 (3张表)
- [ ] 嵌入模型加载成功
- [ ] 用户消息保存到DB
- [ ] 向量生成成功 (384维)
- [ ] 向量保存到DB
- [ ] AI 回复保存到DB
- [ ] 工具调用结果保存
- [ ] 向量相似度检索工作
- [ ] 后续问题能理解上下文 ✅
- [ ] 清除对话功能正常
- [ ] 数据隔离（不同会话）

## 🎉 成功标志

如果您看到：
```
用户: 鹰潭的天气怎么样？
AI: 鹰潭今天天气晴朗，温度25°C...

用户: 适合什么活动？
AI: ✅ 根据鹰潭今天25°C的晴朗天气，非常适合户外活动...
```

**恭喜！RAG 系统工作正常！** 🎊

## 📚 下一步

1. **优化检索**：调整 TopK 数量（当前5条）
2. **添加索引**：为大量数据添加数据库索引
3. **监控性能**：添加性能监控和日志
4. **扩展功能**：
   - 导出对话历史
   - 搜索历史对话
   - 跨会话检索
   - 对话摘要

## 🆘 需要帮助？

查看详细文档：
- **RAG向量数据库实现指南.md** - 完整技术文档
- **多轮对话上下文增强解决方案.md** - 方案对比

---

**开始享受您的 AI 智能助手吧！** 🤖✨



