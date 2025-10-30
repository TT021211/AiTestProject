# AI智能助手前端

这是一个现代化的AI聊天应用前端，与您的Spring Boot后端完美配合。

## 功能特性

### 🚀 核心功能
- **实时聊天**: 基于WebSocket的实时双向通信
- **智能助手**: 集成Ollama AI模型，支持自然语言对话
- **工具调用**: 支持天气查询等工具调用功能
- **自动重连**: 网络断开时自动尝试重连

### 🎨 界面特性
- **现代化设计**: 采用渐变色彩和毛玻璃效果
- **响应式布局**: 完美适配桌面端和移动端
- **流畅动画**: 消息发送、加载状态等动画效果
- **暗色主题**: 优雅的视觉体验

### 💡 用户体验
- **快速操作**: 预设的天气查询快捷按钮
- **智能输入**: 自动调整输入框高度，支持多行输入
- **状态指示**: 实时显示连接状态
- **错误处理**: 友好的错误提示和恢复机制

## 文件结构

```
frontend/
├── index.html          # 主页面
├── styles.css          # 样式文件
├── script.js           # JavaScript逻辑
└── README.md           # 说明文档
```

## 使用方法

### 1. 启动后端服务
确保您的Spring Boot应用正在运行（默认端口8080）

### 2. 启动前端服务（推荐）

#### 方法A：使用Python服务器
```bash
# Windows
start-frontend.bat

# Linux/Mac
chmod +x start-frontend.sh
./start-frontend.sh
```

#### 方法B：使用Node.js服务器
```bash
cd frontend
node server.js
```

#### 方法C：直接打开HTML文件
直接打开 `frontend/index.html` 文件（已自动适配file协议）

### 3. 访问应用
- 如果使用服务器：访问 `http://localhost:3000`
- 如果直接打开HTML：直接打开 `frontend/index.html`

### 4. 开始聊天
- 在输入框中输入您的问题
- 点击发送按钮或按Enter键发送
- 使用快捷按钮快速查询天气

## 技术栈

- **HTML5**: 语义化标签，无障碍访问
- **CSS3**: Flexbox布局，CSS Grid，动画效果
- **JavaScript ES6+**: 类语法，模块化编程
- **WebSocket**: 实时双向通信
- **Font Awesome**: 图标库

## 浏览器兼容性

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 12+
- ✅ Edge 79+

## 自定义配置

### 修改WebSocket连接地址
在 `script.js` 文件中修改连接逻辑：

```javascript
const wsUrl = `${protocol}//${host}/chat`;
```

### 修改样式主题
在 `styles.css` 文件中修改CSS变量：

```css
:root {
    --primary-color: #667eea;
    --secondary-color: #764ba2;
    --success-color: #51cf66;
    --error-color: #ff6b6b;
}
```

### 添加新的快捷操作
在 `index.html` 中添加新的快捷按钮：

```html
<button class="quick-btn" onclick="sendQuickMessage('新功能')">
    <i class="fas fa-icon"></i>
    新功能
</button>
```

## 开发说明

### 项目特点
1. **纯前端实现**: 无需构建工具，直接运行
2. **模块化设计**: 代码结构清晰，易于维护
3. **错误处理**: 完善的异常处理和用户提示
4. **性能优化**: 防抖处理，内存管理

### 扩展功能
- 可以添加文件上传功能
- 支持语音输入输出
- 集成更多AI工具
- 添加用户认证系统

## 故障排除

### 常见问题

1. **连接失败**
   - 检查后端服务是否运行
   - 确认端口8080未被占用
   - 检查防火墙设置

2. **消息发送失败**
   - 检查WebSocket连接状态
   - 查看浏览器控制台错误信息
   - 尝试刷新页面重连

3. **样式显示异常**
   - 确认CSS文件路径正确
   - 检查Font Awesome CDN是否可访问
   - 清除浏览器缓存

### 调试模式
打开浏览器开发者工具，查看控制台输出：

```javascript
// 启用详细日志
localStorage.setItem('debug', 'true');
```

## 更新日志

### v1.0.0 (2024-01-XX)
- ✨ 初始版本发布
- 🚀 基础聊天功能
- 🎨 现代化UI设计
- 📱 响应式布局
- 🔧 工具调用支持

## 许可证

MIT License - 详见 LICENSE 文件

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

---

**享受与AI的智能对话吧！** 🤖✨

