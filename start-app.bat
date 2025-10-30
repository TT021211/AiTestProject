@echo off
REM 设置代码页为 UTF-8 (65001)
chcp 65001 >nul

echo 正在启动应用（UTF-8 编码已启用）...
echo Starting application with UTF-8 encoding...

REM 使用 mvnw 运行 Spring Boot 应用，并设置 JVM 编码参数
call mvnw.cmd spring-boot:run -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8

pause

