@echo off
setlocal
cd /d %~dp0
title Pink-Gold Dream - frontend (5173)

echo ============================================
echo   Pink-Gold Dream frontend starter
echo   Project dir: %~dp0
echo ============================================

rem ===== 1. Locate Node.js / npm =====
where npm >nul 2>&1
if errorlevel 1 (
    set "NODE_FOUND="
    for %%d in (
        "D:\Program Files\nodejs"
        "C:\Program Files\nodejs"
        "%LOCALAPPDATA%\Programs\nodejs"
        "C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-2"
        "C:\Users\HUAWEI\.workbuddy\binaries\node\versions\22.22.2-6"
    ) do if not defined NODE_FOUND if exist "%%~d\npm.cmd" (
        set "NODE_FOUND=%%~d"
        set "PATH=%%~d;%PATH%"
    )
    if not defined NODE_FOUND (
        for /d %%n in ("C:\Users\Administrator\.workbuddy\binaries\node\versions\*") do (
            if not defined NODE_FOUND if exist "%%~n\npm.cmd" (
                set "NODE_FOUND=%%~n"
                set "PATH=%%~n;%PATH%"
            )
        )
    )
    if not defined NODE_FOUND (
        for /d %%n in ("C:\Users\HUAWEI\.workbuddy\binaries\node\versions\*") do (
            if not defined NODE_FOUND if exist "%%~n\npm.cmd" (
                set "NODE_FOUND=%%~n"
                set "PATH=%%~n;%PATH%"
            )
        )
    )
)
where npm >nul 2>&1
if errorlevel 1 (
    echo [ERR] Node.js not found. Please install Node.js 18+ first.
    pause
    exit /b 1
)
echo [1/2] Node.js OK

rem ===== 2. install deps if vite missing (CN mirror, fallback to npmjs) =====
if not exist "%~dp0node_modules\.bin\vite" (
    echo [info] vite missing, installing dependencies ...
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
echo [2/2] dependencies ready

echo.
echo Starting frontend http://192.168.68.22:5173 ...
call npm run dev
pause
