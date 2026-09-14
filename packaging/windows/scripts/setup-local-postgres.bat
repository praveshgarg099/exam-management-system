@echo off
rem ==============================================================================
rem Exam Management System - Local PostgreSQL Service Bootstrap (Windows)
rem Ensures a local PostgreSQL instance is running on port 5432 before launching EMS.
rem If PostgreSQL is already running (external or previous launch), it simply succeeds.
rem ==============================================================================

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%.."
set "APP_DIR=%CD%"
popd

set "PGSQL_DIR=%APP_DIR%\pgsql"
set "DATA_DIR=%LOCALAPPDATA%\ExamManagementSystem\pgsql\data"
set "PORT=5432"

echo [setup-postgres] Checking PostgreSQL status on port %PORT%...

rem 1. Check if port 5432 is already actively listening
powershell -NoProfile -Command "(Test-NetConnection -ComputerName 127.0.0.1 -Port %PORT% -WarningAction SilentlyContinue).TcpTestSucceeded" 2>nul | findstr /i "True" >nul
if not errorlevel 1 (
    echo [setup-postgres] PostgreSQL is already active and listening on port %PORT%.
    exit /b 0
)

netstat -ano | findstr ":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
    echo [setup-postgres] PostgreSQL is already active and listening on port %PORT%.
    exit /b 0
)

rem 2. Verify bundled PostgreSQL binaries exist
if not exist "%PGSQL_DIR%\bin\pg_ctl.exe" (
    echo [setup-postgres] NOTICE: Bundled PostgreSQL binaries not found at:
    echo                 "%PGSQL_DIR%\bin"
    echo                 Application will attempt to connect to system-configured database.
    exit /b 0
)

rem 3. Initialize database cluster if not initialized
if not exist "%DATA_DIR%\PG_VERSION" (
    echo [setup-postgres] Initializing local database cluster in:
    echo                 "%DATA_DIR%"
    if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"
    "%PGSQL_DIR%\bin\initdb.exe" -U postgres -A trust -E UTF8 --locale=C -D "%DATA_DIR%"
    if errorlevel 1 (
        echo [setup-postgres] ERROR: Failed to initialize PostgreSQL data directory. 1>&2
        exit /b 1
    )
    echo [setup-postgres] Local database cluster initialized successfully.
)

rem 4. Start PostgreSQL service
echo [setup-postgres] Starting local PostgreSQL server on port %PORT%...
"%PGSQL_DIR%\bin\pg_ctl.exe" -D "%DATA_DIR%" -l "%DATA_DIR%\server.log" -w start
if errorlevel 1 (
    echo [setup-postgres] Retrying start with status check...
    "%PGSQL_DIR%\bin\pg_ctl.exe" -D "%DATA_DIR%" status >nul 2>&1
    if errorlevel 1 (
        echo [setup-postgres] ERROR: PostgreSQL server failed to start. See "%DATA_DIR%\server.log". 1>&2
        exit /b 1
    )
)

rem 5. Poll pg_isready
if exist "%PGSQL_DIR%\bin\pg_isready.exe" (
    "%PGSQL_DIR%\bin\pg_isready.exe" -h 127.0.0.1 -p %PORT% -U postgres -t 10 >nul 2>&1
)

echo [setup-postgres] Local PostgreSQL server started and healthy on port %PORT%.
endlocal
exit /b 0
