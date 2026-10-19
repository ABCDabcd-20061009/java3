@echo off
chcp 936 >nul
setlocal
cd /d %~dp0
title 粉金梦境-前端页面(5173)

echo ============================================
echo   粉金梦境 前端启动脚本
echo   项目目录: %~dp0
echo ============================================

rem ===== 1. 检查 Node.js =====
where npm >nul 2>&1
if errorlevel 1 (
    if exist "D:\Program Files\nodejs" set "PATH=D:\Program Files\nodejs;%PATH%"
)
where npm >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Node.js，请先安装 Node.js 18+
    pause
    exit /b 1
)
echo [1/2] Node.js 检查通过

rem ===== 2. node_modules 不存在则安装依赖（国内镜像） =====
if not exist "%~dp0node_modules\" (
    echo [提示] node_modules 不存在，正在安装依赖（约 1 分钟）...
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)
echo [2/2] 依赖就绪

echo.
echo 启动前端 http://localhost:5173 ...
call npm run dev
pause
