@echo off
rem ===== 0. 防闪退：非子进程模式时用 cmd /k 重新拉起自身，保证黑窗口永不自动关闭 =====
if /i not "%~1"=="__child" (
    cmd /k ""%~f0" __child"
    goto :eof
)
chcp 936 >nul
setlocal
cd /d %~dp0
title 粉金梦境-后端服务(8081)

rem ===== 0.1 初始化日志文件（即使窗口被关掉也能事后排查）=====
set "LOGDIR=%~dp0logs"
set "LOGFILE=%LOGDIR%\backend-launch.log"
if not exist "%LOGDIR%" md "%LOGDIR%" 2>nul
>>"%LOGFILE%" echo.
>>"%LOGFILE%" echo ===== [%date% %time%] 启动后端 =====

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
    >>"%LOGFILE%" echo [ERR] Java 不可用  JAVA_HOME=%JAVA_HOME%
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
    >>"%LOGFILE%" echo [ERR] MySQL 3306 未监听，服务启动失败
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
        >>"%LOGFILE%" echo [ERR] dream_login 初始化失败（检查 root 密码是否为 123456）
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
        >>"%LOGFILE%" echo [ERR] dream_home.sql 执行失败
        pause
        exit /b 1
    )
)
echo [4/5] table t_dream ready
>>"%LOGFILE%" echo [4/5] t_dream OK

rem ===== 5. Locate Maven & Start Spring Boot =====
set "MVN_BIN="
rem 1) 直接按已知缓存路径命中（最快、最可靠）
for %%p in (
    "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd"
    "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
) do if not defined MVN_BIN if exist "%%~p" set "MVN_BIN=%%~p"
rem 2) 递归搜索 ~/.m2/wrapper/dists 下任意 mvn.cmd
if not defined MVN_BIN (
    for /f "delims=" %%m in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
        if not defined MVN_BIN set "MVN_BIN=%%m"
    )
)
rem 3) 项目自带 mvnw.cmd（.mvn 已恢复）
if not defined MVN_BIN (
    if exist "%~dp0.mvn\wrapper\maven-wrapper.properties" set "MVN_BIN=%~dp0mvnw.cmd"
)
rem 4) 系统 PATH 上的 mvn
if not defined MVN_BIN (
    where mvn.cmd >nul 2>&1 && set "MVN_BIN=mvn.cmd"
)
>>"%LOGFILE%" echo [5/5] Maven定位结果=%MVN_BIN%
if not defined MVN_BIN (
    echo [ERR] Maven not found. Install Maven or restore the project mvnw wrapper (.mvn).
    >>"%LOGFILE%" echo [ERR] Maven 定位失败：缓存/mvnw/PATH 均未命中
    pause
    exit /b 1
)
echo [5/5] Using Maven: %MVN_BIN%
>>"%LOGFILE%" echo [5/5] Maven=%MVN_BIN%
cd /d %~dp0

rem 检查 8081 是否被占用，避免 Spring Boot 启动到一半才报端口冲突
netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo.
    echo [WARN] 端口 8081 已被占用，占用情况如下：
    netstat -ano | findstr ":8081" | findstr "LISTENING"
    echo.
    echo 若这是上一次没关掉的后端，请先运行 停止后端.bat 后再启动。
    >>"%LOGFILE%" echo [WARN] 端口 8081 已被占用，启动中止
    pause
    exit /b 1
)

echo Starting backend http://localhost:8081 ...
echo First start downloads dependencies, please wait.
echo.
rem 用 cmd /c 包裹启动：避免 mvn.cmd 失败时直接退出整个命令行窗口（表现为闪退，看不到报错）
cmd /c "%MVN_BIN% spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
set MVN_RC=%ERRORLEVEL%
>>"%LOGFILE%" echo Spring Boot 退出码=%MVN_RC%
echo.
echo [ 后端进程已退出，退出码 = %MVN_RC% ]
if not "%MVN_RC%"=="0" (
    echo [排查] 常见原因：① MySQL 未启动（脚本已尝试 net start，失败请手动启动 MySQL80 服务）；② 端口 8081 被占用；③ 依赖下载失败（检查 aliyun 镜像网络）。
)
pause
