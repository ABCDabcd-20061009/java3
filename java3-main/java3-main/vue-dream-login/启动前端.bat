@echo off
chcp 936 >nul
setlocal
cd /d %~dp0
title �۽��ξ�-ǰ��ҳ��(5173)

echo ============================================
echo   �۽��ξ� ǰ�������ű�
echo   ��ĿĿ¼: %~dp0
echo ============================================

rem ===== 1. ��� Node.js =====
where npm >nul 2>&1
if errorlevel 1 (
    set "NODE_FOUND="
    for %%d in (
        "D:\Program Files\nodejs"
        "C:\Program Files\nodejs"
        "%LOCALAPPDATA%\Programs\nodejs"
        "C:\Users\HUAWEI\.workbuddy\binaries\node\versions\22.22.2-6"
    ) do if not defined NODE_FOUND if exist "%%~d\npm.cmd" (
        set "NODE_FOUND=%%~d"
        set "PATH=%%~d;%PATH%"
    )
    if not defined NODE_FOUND (
        for /f "delims=" %%n in ('dir /b /ad "C:\Users\HUAWEI\.workbuddy\binaries\node\versions\*" 2^>nul') do (
            if exist "C:\Users\HUAWEI\.workbuddy\binaries\node\versions\%%n\npm.cmd" (
                set "NODE_FOUND=C:\Users\HUAWEI\.workbuddy\binaries\node\versions\%%n"
                set "PATH=C:\Users\HUAWEI\.workbuddy\binaries\node\versions\%%n;%PATH%"
            )
        )
    )
)
where npm >nul 2>&1
if errorlevel 1 (
    echo [����] δ�ҵ� Node.js�����Ȱ�װ Node.js 18+
    pause
    exit /b 1
)
echo [1/2] Node.js ���ͨ��

rem ===== 2. install deps if vite missing (CN mirror, fallback to npmjs) =====
if not exist "%~dp0node_modules\.bin\vite" (
    echo [info] vite ����ȱʧ���ȱ, installing deps ...
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
echo [2/2] ��������

echo.
echo ����ǰ�� http://localhost:5173 ...
call npm run dev
pause
