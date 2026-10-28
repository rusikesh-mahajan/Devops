@echo off
setlocal enabledelayedexpansion
title Automated Residential Visitor Gate-Pass System - Launcher

echo =============================================================
echo   Automated Residential Visitor Gate-Pass System Launcher
echo =============================================================
echo.

:: Detect working directory containing docker-compose.yml
cd /d "%~dp0"
if not exist "docker-compose.yml" (
    if exist "visitor-gate-pass\docker-compose.yml" (
        cd /d "%~dp0visitor-gate-pass"
    )
)

:: 1. Check if Docker is installed and running
echo [1/3] Checking Docker engine status...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Docker is not running or not installed!
    echo Please start Docker Desktop and run this script again.
    echo.
    pause
    exit /b 1
)
echo [OK] Docker engine is active.
echo.

:: 2. Start services via Docker Compose
echo [2/3] Starting Docker Compose stack (Spring Boot App + MySQL)...
docker compose -p visitor-gate-pass up -d
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start Docker Compose containers.
    pause
    exit /b 1
)
echo [OK] Containers started.
echo.

:: 3. Wait for Application Health Endpoint
echo [3/3] Waiting for application to initialize (checking /actuator/health)...
set /a attempts=0
set /a max_attempts=30

:WAIT_LOOP
set /a attempts+=1
curl.exe -s http://localhost:8080/actuator/health | findstr /i "UP" >nul 2>&1
if %errorlevel% equ 0 (
    goto APP_READY
)

if !attempts! geq !max_attempts! (
    echo.
    echo [WARN] Health check timed out after 60 seconds.
    echo Containers may still be initializing. Attempting to open browser...
    goto OPEN_BROWSER
)

<nul set /p =.
timeout /t 2 /nobreak >nul
goto WAIT_LOOP

:APP_READY
echo.
echo [OK] Application is UP and HEALTHY!

:OPEN_BROWSER
echo.
echo =============================================================
echo               PROJECT IS RUNNING SUCCESSFULLY!
echo =============================================================
echo   Application URL: http://localhost:8080/dashboard
echo   Health Endpoint: http://localhost:8080/actuator/health
echo.
echo   DEFAULT CREDENTIALS:
echo   ----------------------------------------------------------
echo   Role            Username    Password
echo   Gate Guard:     guard1      guard123
echo   Resident:       resident1   res123
echo   Administrator:  admin       admin123
echo =============================================================
echo.
echo Opening browser...
start http://localhost:8080/dashboard

echo.
echo Press any key to stop the containers, or close this window to keep them running in background.
pause >nul

echo.
echo Stopping containers...
docker compose -p visitor-gate-pass stop
echo [OK] Containers stopped cleanly.
timeout /t 3 >nul
exit /b 0
