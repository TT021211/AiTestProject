# PowerShell 启动脚本 - 修复中文乱码
# 设置控制台编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 设置代码页为 UTF-8
chcp 65001 | Out-Null

Write-Host "正在启动应用（UTF-8 编码已启用）..." -ForegroundColor Green
Write-Host "Starting application with UTF-8 encoding..." -ForegroundColor Green
Write-Host ""

# 运行 Maven Spring Boot 插件
& .\mvnw.cmd spring-boot:run `
    -Dfile.encoding=UTF-8 `
    -Dconsole.encoding=UTF-8 `
    -Duser.language=zh `
    -Duser.country=CN




