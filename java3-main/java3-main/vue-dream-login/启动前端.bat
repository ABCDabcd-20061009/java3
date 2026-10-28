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

rem ===== 2. install deps if vite missing (CN mirror, fallback to npmjs) =====
if not exist "%~dp0node_modules\.bin\vite" (
    echo [info] vite 依赖缺失或残缺, installing deps ...
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo [warn] npmmirror failed, try registry.npmjs.org ...
        call npm install --registry=https://registry.npmjs.org
    )
    if errorlevel 1 (
        echo [error] dependency install failed
        pause
        exit /b 1
    )
    if not exist "%~dp0node_modules\.bin\vite" (
        echo [error] vite still missing after install
        pause
        exit /b 1
    )
)
echo [2/2] 依赖就绪

echo.
echo 启动前端 http://localhost:5173 ...
call npm run dev
pause
