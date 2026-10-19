@echo off
chcp 936 >nul
setlocal
cd /d %~dp0
title 粉金梦境-后端服务(8081)

echo ============================================
echo   Pink-Gold Dream backend starter
echo   Project dir: %~dp0
echo ============================================

rem ===== 1. Check Java =====
if not defined JAVA_HOME (
    if exist "C:\Program Files\Zulu\zulu-21" set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"
)
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERR] Java not found. Install JDK 17+ or set JAVA_HOME.
    pause
    exit /b 1
)
echo [1/5] Java OK

rem ===== 2. Check MySQL(3306), try to start if not running =====
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [INFO] MySQL not running, trying to start service...
    net start MySQL80 >nul 2>&1
    if errorlevel 1 net start MySQL >nul 2>&1
    timeout /t 3 /nobreak >nul
)
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [ERR] MySQL failed to start. Start MySQL service manually.
    pause
    exit /b 1
)
echo [2/5] MySQL running

rem ===== 3. Ensure database dream_login exists (init if missing) =====
set "MYSQL_EXE=mysql"
where mysql >nul 2>&1
if errorlevel 1 set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
"%MYSQL_EXE%" -uroot -p123456 -e "USE dream_login" >nul 2>&1
if errorlevel 1 (
    echo [INFO] database dream_login missing, running init SQL...
    "%MYSQL_EXE%" -uroot -p123456 --default-character-set=utf8mb4 < "%~dp0sql\dream_login.sql"
    if errorlevel 1 (
        echo [ERR] DB init failed. Check MySQL account/password.
        pause
        exit /b 1
    )
)
echo [3/5] database dream_login ready

rem ===== 4. Ensure homepage tables: t_dream / t_dream_category / t_dream_like / t_dream_search_log / t_dream_favorite =====
"%MYSQL_EXE%" -uroot -p123456 dream_login -e "SELECT 1 FROM t_dream_favorite LIMIT 1" >nul 2>&1
if errorlevel 1 (
    echo [INFO] homepage tables missing, running dream_home.sql...
    "%MYSQL_EXE%" -uroot -p123456 --default-character-set=utf8mb4 dream_login < "%~dp0sql\dream_home.sql"
    if errorlevel 1 (
        echo [ERR] t_dream init failed.
        pause
        exit /b 1
    )
)
echo [4/5] table t_dream ready

rem ===== 5. Start Spring Boot =====
echo [5/5] Starting backend http://localhost:8081 ...
echo First start downloads Maven and dependencies, please wait.
echo.
call mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
pause
