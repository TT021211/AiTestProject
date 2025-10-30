#!/bin/bash
echo "启动前端服务器..."
echo "请在浏览器中访问: http://localhost:3000"
echo "按 Ctrl+C 停止服务器"
echo

cd frontend
python3 -m http.server 3000


