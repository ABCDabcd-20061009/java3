@echo off
chcp 65001 >nul
REM ============================================================
REM  Drama cover restore tool
REM  Copies the 9 backup covers back into public/drama-media/covers
REM  Use this if the covers are ever deleted or the project is re-extracted.
REM  Backup sources are tried in order until one is found.
REM ============================================================

set DST=%~dp0public\drama-media\covers
if not exist "%DST%" mkdir "%DST%"

set FOUND=0

if exist "D:\workSpace\drama-covers-backup\resized\*.webp" (
  copy /Y "D:\workSpace\drama-covers-backup\resized\*.webp" "%DST%\" >nul
  set FOUND=1
  echo [1/1] Restored from D:\workSpace\drama-covers-backup\resized
  goto done
)

if exist "%USERPROFILE%\Pictures\drama-covers-backup\resized\*.webp" (
  copy /Y "%USERPROFILE%\Pictures\drama-covers-backup\resized\*.webp" "%DST%\" >nul
  set FOUND=1
  echo [1/1] Restored from %USERPROFILE%\Pictures\drama-covers-backup\resized
  goto done
)

if exist "%~dp0public\drama-media\covers-backup\resized\*.webp" (
  copy /Y "%~dp0public\drama-media\covers-backup\resized\*.webp" "%DST%\" >nul
  set FOUND=1
  echo [1/1] Restored from local covers-backup
  goto done
)

:done
if "%FOUND%"=="1" (
  echo.
  echo Done. 9 covers restored. Refresh the page.
) else (
  echo.
  echo ERROR: No backup found. Please check backup folders.
)
echo.
pause
