@echo off
REM MCP API 测试脚本 (Windows)

set BASE_URL=http://localhost:8080
set MCP_URL=%BASE_URL%/api/mcp

echo =========================================
echo MCP API 测试脚本
echo =========================================
echo.

REM 1. 健康检查
echo [1] 健康检查
curl -s "%MCP_URL%/health"
echo.
echo.

REM 2. 获取服务器信息
echo [2] 获取服务器信息
curl -s "%MCP_URL%/info"
echo.
echo.

REM 3. 初始化
echo [3] 初始化
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"initialize\",\"params\":{}}"
echo.
echo.

REM 4. Ping
echo [4] Ping
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"method\":\"ping\",\"params\":{}}"
echo.
echo.

REM 5. 列出工具
echo [5] 列出工具
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"3\",\"method\":\"tools/list\",\"params\":{}}"
echo.
echo.

REM 6. 调用天气工具
echo [6] 调用天气工具 - 北京
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"4\",\"method\":\"tools/call\",\"params\":{\"name\":\"get_weather\",\"arguments\":{\"city\":\"北京\"}}}"
echo.
echo.

REM 7. 调用股票工具
echo [7] 调用股票工具 - AAPL
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"5\",\"method\":\"tools/call\",\"params\":{\"name\":\"get_stock\",\"arguments\":{\"ticker\":\"AAPL\"}}}"
echo.
echo.

REM 8. 列出资源
echo [8] 列出资源
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"6\",\"method\":\"resources/list\",\"params\":{}}"
echo.
echo.

REM 9. 读取系统提示词
echo [9] 读取系统提示词
curl -s -X POST "%MCP_URL%" ^
  -H "Content-Type: application/json" ^
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"7\",\"method\":\"resources/read\",\"params\":{\"uri\":\"prompt://system/assistant\"}}"
echo.
echo.

echo =========================================
echo 测试完成！
echo =========================================
pause








