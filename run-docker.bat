@echo off
rem ==============================================================================
rem Exam Management System - One-Click Docker Runner (Windows)
rem ==============================================================================

where docker >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH.
    echo Please install Docker Desktop from https://www.docker.com/
    pause
    exit /b 1
)

echo ============================================================
echo     Starting Exam Management System in Docker Container     
echo ============================================================

docker compose up -d --build
if errorlevel 1 (
    echo ERROR: Failed to start Docker container.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo SUCCESS: Container is running!
echo Open the Exam Management System in your browser at:
echo.
echo     http://localhost:8080
echo.
echo To stop the container, run: docker compose down
echo ============================================================
pause
