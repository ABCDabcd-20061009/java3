@echo off
chcp 936 >nul
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

rem ===== 3. 探测本机局域网 IP（取 ipconfig 里第一个 IPv4，跳过回环）=====
set "LANIP="
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    if not defined LANIP set "LANIP=%%a"
)
if defined LANIP set "LANIP=%LANIP: =%"
if not defined LANIP set "LANIP=127.0.0.1"

echo.
echo ============================================
echo   本机访问  : http://localhost:5173/
echo   局域网访问: http://%LANIP%:5173/
echo               （同一 WiFi/交换机下的手机、其它电脑直接打开这个地址）
echo   后端接口  : http://%LANIP%:8081/api  （请先双击 启动后端.bat）
echo ============================================
echo.

rem ===== 4. 5173 已经在跑（之前开过没关）就别重复启动，直接开浏览器 =====
netstat -ano | findstr /c:":5173" | findstr /c:"LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [info] 前端已经在运行，直接打开浏览器 http://%LANIP%:5173/
    echo        如果需要重启，先双击 停止前端.bat
    start "" http://%LANIP%:5173/
    ping -n 3 127.0.0.1 >nul
    exit /b 0
)

echo 正在启动 Vite，约 6 秒后自动用默认浏览器打开 http://%LANIP%:5173/
echo 若浏览器没弹出，手动复制上面的「局域网访问」地址打开即可。
echo.

rem 延迟 6 秒（ping 6 次 ≈ 6 秒，比 timeout 更稳：不受输入重定向影响）再打开浏览器，
rem 等 Vite 真正就绪，避免刚启动就打开显示"无法访问"
start "" /b cmd /c "ping -n 7 127.0.0.1 >nul & start http://%LANIP%:5173/"

call npm run dev
pause
