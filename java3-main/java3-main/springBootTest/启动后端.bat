@echo off
rem ===== 0. Relaunch guard: when double-clicked, re-open under "cmd /k" so the window stays open =====
if /i not "%~1"=="__child" (
    cmd /k ""%~f0" __child"
    goto :eof
)
setlocal
cd /d %~dp0
title Pink-Gold Dream - backend (8081)

rem ===== 0.1 Init log file (always local, easy troubleshooting) =====
set "LOGDIR=%~dp0logs"
set "LOGFILE=%LOGDIR%\backend-launch.log"
if not exist "%LOGDIR%" md "%LOGDIR%" 2>nul
>>"%LOGFILE%" echo.
>>"%LOGFILE%" echo ===== [%date% %time%] backend launch =====

echo ============================================
echo   Pink-Gold Dream backend starter
echo   Project dir: %~dp0
echo ============================================

rem ===== 1. Check Java (prefer known Zulu JDK; a JRE-only JAVA_HOME breaks spring-boot:run) =====
set "JAVA_HOME="
for %%d in (
    "C:\Program Files\Zulu\zulu-21"
    "C:\Program Files\Java"
    "D:\IntelliJ IDEA 2024.3.2.2\jbr"
    "C:\Program Files\JetBrains\IntelliJ IDEA\jbr"
) do if not defined JAVA_HOME if exist "%%~d\bin\java.exe" set "JAVA_HOME=%%~d"
if not defined JAVA_HOME (
    for /f "delims=" %%j in ('dir /b /ad "D:\IntelliJ IDEA*" 2^>nul') do (
        if exist "D:\%%j\jbr\bin\java.exe" set "JAVA_HOME=D:\%%j\jbr"
    )
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERR] Java not found. Install JDK 17+ or set JAVA_HOME.
    >>"%LOGFILE%" echo [ERR] Java unavailable JAVA_HOME=%JAVA_HOME%
    pause
    exit /b 1
)
echo [1/5] Java OK
>>"%LOGFILE%" echo [1/5] Java OK  JAVA_HOME=%JAVA_HOME%

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
    >>"%LOGFILE%" echo [ERR] MySQL 3306 not listening, auto start failed
    pause
    exit /b 1
)
echo [2/5] MySQL running
>>"%LOGFILE%" echo [2/5] MySQL OK

rem ===== 3. Ensure database dream_login exists (init if missing) =====
set "MYSQL_EXE=mysql"
where mysql >nul 2>&1
if errorlevel 1 set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
>>"%LOGFILE%" echo [3/5] mysql.exe=%MYSQL_EXE%
"%MYSQL_EXE%" -uroot -p123456 -e "USE dream_login" >nul 2>&1
if errorlevel 1 (
    echo [INFO] database dream_login missing, running init SQL...
    "%MYSQL_EXE%" -uroot -p123456 --default-character-set=utf8mb4 < "%~dp0sql\dream_login.sql"
    if errorlevel 1 (
        echo [ERR] DB init failed. Check MySQL account/password.
        >>"%LOGFILE%" echo [ERR] dream_login init failed, check root password is 123456
        pause
        exit /b 1
    )
)
echo [3/5] database dream_login ready
>>"%LOGFILE%" echo [3/5] dream_login OK

rem ===== 4. Ensure homepage tables: t_dream / t_dream_category / t_dream_like / t_dream_search_log / t_dream_favorite =====
"%MYSQL_EXE%" -uroot -p123456 dream_login -e "SELECT 1 FROM t_dream_favorite LIMIT 1" >nul 2>&1
if errorlevel 1 (
    echo [INFO] homepage tables missing, running dream_home.sql...
    "%MYSQL_EXE%" -uroot -p123456 --default-character-set=utf8mb4 dream_login < "%~dp0sql\dream_home.sql"
    if errorlevel 1 (
        echo [ERR] t_dream init failed.
        >>"%LOGFILE%" echo [ERR] dream_home.sql execution failed
        pause
        exit /b 1
    )
)
echo [4/5] table t_dream ready
>>"%LOGFILE%" echo [4/5] t_dream OK

rem ===== 4.5 Ensure broadcast-drama tables: t_drama / t_drama_category =====
"%MYSQL_EXE%" -uroot -p123456 dream_login -e "SELECT 1 FROM t_drama LIMIT 1" >nul 2>&1
if errorlevel 1 (
    echo [INFO] drama tables missing, running dream_drama.sql...
    "%MYSQL_EXE%" -uroot -p123456 --default-character-set=utf8mb4 dream_login < "%~dp0sql\dream_drama.sql"
    if errorlevel 1 (
        echo [ERR] t_drama init failed.
        >>"%LOGFILE%" echo [ERR] dream_drama.sql execution failed
        pause
        exit /b 1
    )
)
echo [4.5/5] table t_drama ready
>>"%LOGFILE%" echo [4.5/5] t_drama OK

rem ===== 5. Locate Maven & Start Spring Boot =====
set "MVN_BIN="
rem 1) known wrapper cache paths first (fast, reliable)
for %%p in (
    "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd"
    "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
) do if not defined MVN_BIN if exist "%%~p" set "MVN_BIN=%%~p"
rem 2) recursive search under ~/.m2/wrapper/dists for any mvn.cmd
if not defined MVN_BIN (
    for /f "delims=" %%m in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
        if not defined MVN_BIN set "MVN_BIN=%%m"
    )
)
rem 3) fall back to project-bundled mvnw.cmd (requires .mvn dir)
if not defined MVN_BIN (
    if exist "%~dp0.mvn\wrapper\maven-wrapper.properties" set "MVN_BIN=%~dp0mvnw.cmd"
)
rem 4) mvn on system PATH
if not defined MVN_BIN (
    where mvn.cmd >nul 2>&1 && set "MVN_BIN=mvn.cmd"
)
>>"%LOGFILE%" echo [5/5] Maven resolved=%MVN_BIN%
if not defined MVN_BIN (
    echo [ERR] Maven not found. Install Maven or restore the project mvnw wrapper.
    >>"%LOGFILE%" echo [ERR] Maven not found in .m2/mvnw/PATH
    pause
    exit /b 1
)
echo [5/5] Using Maven: %MVN_BIN%
>>"%LOGFILE%" echo [5/5] Maven=%MVN_BIN%
cd /d %~dp0

rem Check 8081 occupation to avoid port conflict with another Spring Boot instance
netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo.
    echo [WARN] Port 8081 is already in use:
    netstat -ano | findstr ":8081" | findstr "LISTENING"
    echo.
    echo Another backend is still running. Run the stop-backend script in the same folder first.
    >>"%LOGFILE%" echo [WARN] Port 8081 occupied, launch aborted
    pause
    exit /b 1
)

echo Starting backend http://192.168.68.22:8081 ...
echo First start downloads dependencies, please wait.
echo.
rem Run via cmd /c so that if mvn.cmd fails the process exits cleanly instead of leaving a zombie window
cmd /c "%MVN_BIN% spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
set MVN_RC=%ERRORLEVEL%
>>"%LOGFILE%" echo Spring Boot exit code=%MVN_RC%
echo.
echo [ Backend stopped. Exit code = %MVN_RC% ]
if not "%MVN_RC%"=="0" (
    echo [TIPS] Possible causes: 1 MySQL not running; 2 port 8081 occupied; 3 dependency download failed, check network.
)
pause
