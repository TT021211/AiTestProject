class ChatApp {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.messageHistory = [];
        this.isTyping = false;
        
        this.initElements();
        this.initEventListeners();
        this.connectWebSocket();
    }

    initElements() {
        this.messagesContainer = document.getElementById('messagesContainer');
        this.messageInput = document.getElementById('messageInput');
        this.sendButton = document.getElementById('sendButton');
        this.clearContextBtn = document.getElementById('clearContextBtn');
        this.statusIndicator = document.getElementById('status-indicator');
        this.statusText = document.getElementById('status-text');
        this.charCount = document.getElementById('charCount');
        this.loadingOverlay = document.getElementById('loadingOverlay');
        this.errorToast = document.getElementById('errorToast');
        this.errorMessage = document.getElementById('errorMessage');
    }

    initEventListeners() {
        // 发送按钮点击事件
        this.sendButton.addEventListener('click', () => this.sendMessage());
        
        // 清除上下文按钮点击事件
        this.clearContextBtn.addEventListener('click', () => this.clearContext());
        
        // 输入框事件
        this.messageInput.addEventListener('input', () => this.handleInputChange());
        this.messageInput.addEventListener('keydown', (e) => this.handleKeyDown(e));
        
        // 自动调整输入框高度
        this.messageInput.addEventListener('input', () => this.autoResizeTextarea());
        
        // 窗口关闭时断开连接
        window.addEventListener('beforeunload', () => {
            if (this.socket) {
                this.socket.close();
            }
        });
    }

    connectWebSocket() {
        try {
            // 检测是否为file协议，如果是则使用localhost
            let protocol, host;
            if (window.location.protocol === 'file:') {
                protocol = 'ws:';
                host = 'localhost:8080';
                console.log('检测到file协议，使用localhost:8080');
            } else if (window.location.host === 'localhost:3000') {
                // 如果前端运行在3000端口，后端在8080端口
                protocol = 'ws:';
                host = 'localhost:8080';
                console.log('检测到前端服务器，连接到后端localhost:8080');
            } else {
                protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                host = window.location.host;
            }
            const wsUrl = `${protocol}//${host}/chat`;
            
            console.log('正在连接到:', wsUrl);
            
            this.socket = new WebSocket(wsUrl);
            
            this.socket.onopen = () => {
                console.log('WebSocket连接已建立');
                this.isConnected = true;
                this.updateConnectionStatus('connected', '已连接');
                this.hideError();
            };
            
            this.socket.onmessage = (event) => {
                this.handleMessage(event);
            };
            
            this.socket.onclose = (event) => {
                console.log('WebSocket连接已关闭:', event.code, event.reason);
                this.isConnected = false;
                this.updateConnectionStatus('disconnected', '连接断开');
                
                // 如果不是正常关闭，尝试重连
                if (event.code !== 1000) {
                    setTimeout(() => this.connectWebSocket(), 3000);
                }
            };
            
            this.socket.onerror = (error) => {
                console.error('WebSocket错误:', error);
                this.updateConnectionStatus('error', '连接错误');
                this.showError('连接服务器失败，请检查网络或稍后重试');
            };
            
        } catch (error) {
            console.error('创建WebSocket连接失败:', error);
            this.showError('无法创建连接，请刷新页面重试');
        }
    }

    handleMessage(event) {
        try {
            console.log('收到原始消息:', event.data);
            const data = JSON.parse(event.data);
            console.log('解析后的数据:', data);
            this.hideLoading();
            this.addMessage('assistant', data.response);
        } catch (error) {
            console.error('解析消息失败:', error);
            console.error('原始数据:', event.data);
            this.hideLoading();
            this.addMessage('assistant', '抱歉，我无法理解这个消息。');
        }
    }

    sendMessage() {
        const message = this.messageInput.value.trim();
        if (!message || !this.isConnected) return;
        
        // 添加用户消息
        this.addMessage('user', message);
        
        // 清空输入框
        this.messageInput.value = '';
        this.updateCharCount();
        this.updateSendButton();
        this.autoResizeTextarea();
        
        // 显示加载状态
        this.showLoading();
        
        // 发送到服务器
        try {
            this.socket.send(message);
        } catch (error) {
            console.error('发送消息失败:', error);
            this.hideLoading();
            this.showError('发送消息失败，请重试');
        }
    }

    sendQuickMessage(message) {
        if (!this.isConnected) {
            this.showError('请等待连接建立后再发送消息');
            return;
        }
        
        this.messageInput.value = message;
        this.sendMessage();
    }

    addMessage(type, content) {
        // 移除欢迎消息
        const welcomeMessage = this.messagesContainer.querySelector('.welcome-message');
        if (welcomeMessage) {
            welcomeMessage.remove();
        }
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        
        const time = new Date().toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        messageDiv.innerHTML = `
            <div class="message-content">
                ${this.formatMessage(content)}
                <div class="message-time">${time}</div>
            </div>
        `;
        
        this.messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
        
        // 保存到历史记录
        this.messageHistory.push({ type, content, time });
    }

    formatMessage(content) {
        // 简单的消息格式化
        return content
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>');
    }

    handleInputChange() {
        this.updateCharCount();
        this.updateSendButton();
    }

    handleKeyDown(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            this.sendMessage();
        }
    }

    updateCharCount() {
        const count = this.messageInput.value.length;
        this.charCount.textContent = count;
        
        // 字符数接近限制时改变颜色
        if (count > 900) {
            this.charCount.style.color = '#ff6b6b';
        } else if (count > 800) {
            this.charCount.style.color = '#ffd43b';
        } else {
            this.charCount.style.color = '#666';
        }
    }

    updateSendButton() {
        const hasText = this.messageInput.value.trim().length > 0;
        this.sendButton.disabled = !hasText || !this.isConnected;
    }

    autoResizeTextarea() {
        this.messageInput.style.height = 'auto';
        this.messageInput.style.height = Math.min(this.messageInput.scrollHeight, 120) + 'px';
    }

    updateConnectionStatus(status, text) {
        this.statusIndicator.className = `status-dot ${status}`;
        this.statusText.textContent = text;
    }

    showLoading() {
        this.loadingOverlay.classList.remove('hidden');
    }

    hideLoading() {
        this.loadingOverlay.classList.add('hidden');
    }

    showError(message) {
        this.errorMessage.textContent = message;
        this.errorToast.classList.remove('hidden');
        
        // 5秒后自动隐藏
        setTimeout(() => this.hideError(), 5000);
    }

    hideError() {
        this.errorToast.classList.add('hidden');
    }

    scrollToBottom() {
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    clearContext() {
        if (!this.isConnected) {
            this.showError('请等待连接建立后再清除对话');
            return;
        }

        // 确认对话框
        if (confirm('确定要清除对话历史吗？这将开始一个全新的对话。')) {
            // 清除前端显示的消息
            this.messagesContainer.innerHTML = '';
            this.messageHistory = [];
            
            // 重新显示欢迎消息
            this.messagesContainer.innerHTML = `
                <div class="welcome-message">
                    <div class="welcome-content">
                        <i class="fas fa-sparkles"></i>
                        <h2>欢迎使用AI智能助手</h2>
                        <p>对话已清除，让我们重新开始吧！我可以帮您查询天气信息，或者回答其他问题。</p>
                        <div class="quick-actions">
                            <button class="quick-btn" onclick="sendQuickMessage('A的天气')">
                                <i class="fas fa-sun"></i>
                                A城市天气
                            </button>
                            <button class="quick-btn" onclick="sendQuickMessage('B的天气')">
                                <i class="fas fa-cloud-rain"></i>
                                B城市天气
                            </button>
                        </div>
                    </div>
                </div>
            `;
            
            // 重新连接 WebSocket 以创建新的会话（新的 session ID）
            // 这会触发后端清除旧的上下文
            if (this.socket) {
                this.socket.close();
                setTimeout(() => {
                    this.connectWebSocket();
                    console.log('对话已清除，创建新会话');
                }, 100);
            }
        }
    }

    // 公共方法供HTML调用
    static sendQuickMessage(message) {
        if (window.chatApp) {
            window.chatApp.sendQuickMessage(message);
        }
    }

    static hideError() {
        if (window.chatApp) {
            window.chatApp.hideError();
        }
    }
}

// 全局函数供HTML调用
function sendQuickMessage(message) {
    ChatApp.sendQuickMessage(message);
}

function hideError() {
    ChatApp.hideError();
}

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
    
    // 添加一些实用功能
    console.log('AI聊天应用已启动');
    console.log('支持的功能:');
    console.log('- 实时聊天');
    console.log('- 天气查询 (A的天气, B的天气)');
    console.log('- 响应式设计');
    console.log('- 自动重连');
});

// 处理页面可见性变化
document.addEventListener('visibilitychange', () => {
    if (window.chatApp && !document.hidden && !window.chatApp.isConnected) {
        console.log('页面重新可见，尝试重连...');
        window.chatApp.connectWebSocket();
    }
});

