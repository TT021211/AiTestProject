# AiTestProject

一个基于 Spring Boot 的 AI 测试项目，集成了 MCP（Model Context Protocol）服务器、WebSocket 实时对话、简单 RAG 检索增强与前端演示页面。

## 功能特性
- MCP 服务器：提供工具调用、资源读取等能力（`/mcp/**`）
- WebSocket 对话：与后端 LLM 服务进行流式对话（`/chat`、`/mcp-chat`）
- RAG 能力：向量化存储与相似度检索（`VectorEmbeddingRepository` 等）
- 前端页面：内置简单聊天与 WebSocket 测试页面（`frontend/`）

## 目录结构
- `src/main/java/com/cuixing/aitestproject/`
  - `chat/`：WebSocket 处理与 Ollama 服务对接
  - `mcp/`：MCP 配置、协议、工具与服务实现
  - `service/`：RAG 服务
  - `entity/`、`repository/`：实体与数据访问
- `src/main/resources/`：配置与提示词模板
- `frontend/`：演示用前端页面与静态资源

## 快速开始
### 环境要求
- Java 17+
- Maven 3.8+
- Node.js 18+（仅用于前端本地调试，可选）

### 启动后端
- Windows 直接运行：`start-app.bat` 或 `start-app.ps1`
- 或使用 Maven：
  ```bash
  mvn spring-boot:run
  ```

服务启动后，默认端口见 `src/main/resources/application.properties`。

### 启动前端（可选）
进入 `frontend/`：
```bash
node server.js
```
或直接打开 `frontend/index.html` / `frontend/mcp-chat.html` 测试。

## 关键接口与端点
- WebSocket：
  - `ws://<host>:<port>/chat`
  - `ws://<host>:<port>/mcp-chat`
- MCP 相关：见 `mcp/controller` 与 `mcp/server`，工具在 `mcp/tools` 下定义

## 开发指引
- 修改后端逻辑后，建议运行：
  ```bash
  mvn -q -DskipTests package
  ```
- 常用类：
  - 启动类：`AiTestProjectApplication`
  - WebSocket：`ChatWebSocketHandler`、`McpChatWebSocketHandler`
  - MCP 服务：`McpServer`、`McpOllamaService`
  - RAG 服务：`RagService`

## 常见问题
- Windows 上若出现换行符提示（CRLF/LF），为正常提醒。
- 若推送 GitHub 失败，可考虑：
  - 切换 SSL 后端：`git config --global http.sslBackend schannel`
  - 使用代理：`git config --global https.proxy http://<proxy>:<port>`
  - 改用 SSH：`git remote set-url origin git@github.com:<user>/<repo>.git`

## 许可证
本项目用于学习与演示用途，按仓库 License 文件约束（如未提供 License，请在内部环境使用）。
