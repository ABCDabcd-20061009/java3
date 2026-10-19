@echo off
chcp 936 >nul
setlocal
title 停止后端服务(8081)

set "FOUND=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081" ^| findstr "LISTENING"') do (
    echo 停止进程 PID=%%a ...
    taskkill /PID %%a /F >nul 2>&1
    set "FOUND=1"
)
if "%FOUND%"=="1" (
    echo 后端服务已停止
) else (
    echo 端口 8081 上没有运行中的服务
)
pause
