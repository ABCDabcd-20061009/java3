@echo off
chcp 65001 >nul
title 放行局域网访问 - 梦境网页
echo ============================================
echo   放行前端(5173)和后端(8081)端口
echo   让同一局域网的其他电脑可以访问网页
echo ============================================
echo.
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [提示] 需要管理员权限，正在申请...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)
echo [OK] 已获得管理员权限，正在添加防火墙规则...
echo.

netsh advfirewall firewall delete rule name="Vite Dev 5173" >nul 2>&1
netsh advfirewall firewall add rule name="Vite Dev 5173" dir=in action=allow protocol=TCP localport=5173
echo [OK] 5173 端口已放行

netsh advfirewall firewall delete rule name="SpringBoot 8081" >nul 2>&1
netsh advfirewall firewall add rule name="SpringBoot 8081" dir=in action=allow protocol=TCP localport=8081
echo [OK] 8081 端口已放行

echo.
echo ============================================
echo   完成！其他电脑现在可以访问：
echo   http://192.168.88.102:5173/
echo ============================================
echo.
pause
