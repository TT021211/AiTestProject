#!/bin/bash

# MCP API 测试脚本

BASE_URL="http://localhost:8080"
MCP_URL="$BASE_URL/api/mcp"

echo "========================================="
echo "MCP API 测试脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 测试函数
test_api() {
    local name=$1
    local data=$2
    
    echo -e "${BLUE}[测试] $name${NC}"
    echo "请求: $data"
    
    response=$(curl -s -X POST "$MCP_URL" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    echo "响应:"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
}

# 1. 健康检查
echo -e "${GREEN}1. 健康检查${NC}"
curl -s "$MCP_URL/health" | jq '.' 2>/dev/null || echo "jq未安装，显示原始响应"
echo ""
echo ""

# 2. 获取服务器信息
echo -e "${GREEN}2. 获取服务器信息${NC}"
curl -s "$MCP_URL/info" | jq '.' 2>/dev/null || echo "jq未安装"
echo ""
echo ""

# 3. 初始化
echo -e "${GREEN}3. 初始化${NC}"
test_api "Initialize" '{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "initialize",
  "params": {}
}'

# 4. Ping
echo -e "${GREEN}4. Ping${NC}"
test_api "Ping" '{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "ping",
  "params": {}
}'

# 5. 列出工具
echo -e "${GREEN}5. 列出工具${NC}"
test_api "Tools List" '{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/list",
  "params": {}
}'

# 6. 调用天气工具
echo -e "${GREEN}6. 调用天气工具${NC}"
test_api "Get Weather - Beijing" '{
  "jsonrpc": "2.0",
  "id": "4",
  "method": "tools/call",
  "params": {
    "name": "get_weather",
    "arguments": {
      "city": "北京"
    }
  }
}'

# 7. 调用股票工具
echo -e "${GREEN}7. 调用股票工具${NC}"
test_api "Get Stock - AAPL" '{
  "jsonrpc": "2.0",
  "id": "5",
  "method": "tools/call",
  "params": {
    "name": "get_stock",
    "arguments": {
      "ticker": "AAPL"
    }
  }
}'

# 8. 列出资源
echo -e "${GREEN}8. 列出资源${NC}"
test_api "Resources List" '{
  "jsonrpc": "2.0",
  "id": "6",
  "method": "resources/list",
  "params": {}
}'

# 9. 读取系统提示词资源
echo -e "${GREEN}9. 读取系统提示词${NC}"
test_api "Read Prompt Resource" '{
  "jsonrpc": "2.0",
  "id": "7",
  "method": "resources/read",
  "params": {
    "uri": "prompt://system/assistant"
  }
}'

# 10. 测试错误处理
echo -e "${GREEN}10. 测试错误处理 - 未知方法${NC}"
test_api "Unknown Method" '{
  "jsonrpc": "2.0",
  "id": "8",
  "method": "unknown/method",
  "params": {}
}'

# 11. 测试错误处理 - 无效工具
echo -e "${GREEN}11. 测试错误处理 - 无效工具${NC}"
test_api "Invalid Tool" '{
  "jsonrpc": "2.0",
  "id": "9",
  "method": "tools/call",
  "params": {
    "name": "invalid_tool",
    "arguments": {}
  }
}'

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}测试完成！${NC}"
echo -e "${GREEN}=========================================${NC}"








