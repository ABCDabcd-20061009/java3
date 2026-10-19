@echo off
chcp 936 >nul
setlocal
title 停止前端服务(5173)

set "FOUND=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo 停止进程 PID=%%a ...
    taskkill /PID %%a /F >nul 2>&1
    set "FOUND=1"
)
if "%FOUND%"=="1" (
    echo 前端服务已停止
) else (
    echo 端口 5173 上没有运行中的服务
)
pause
