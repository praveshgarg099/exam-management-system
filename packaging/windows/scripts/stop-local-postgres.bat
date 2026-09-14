@echo off
rem ==============================================================================
rem Exam Management System - Local PostgreSQL Service Shutdown (Windows)
rem Gracefully stops the local PostgreSQL server instance on application exit or uninstall.
rem ==============================================================================

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%.."
set "APP_DIR=%CD%"
popd

set "PGSQL_DIR=%APP_DIR%\pgsql"
set "DATA_DIR=%LOCALAPPDATA%\ExamManagementSystem\pgsql\data"

if not exist "%DATA_DIR%\postmaster.pid" (
    echo [stop-postgres] Local PostgreSQL server is not currently running.
    exit /b 0
)

if not exist "%PGSQL_DIR%\bin\pg_ctl.exe" (
    echo [stop-postgres] Notice: pg_ctl.exe not found at "%PGSQL_DIR%\bin".
    exit /b 0
)

echo [stop-postgres] Stopping local PostgreSQL server...
"%PGSQL_DIR%\bin\pg_ctl.exe" -D "%DATA_DIR%" -m fast stop

if errorlevel 1 (
    echo [stop-postgres] Normal stop timed out. Forcing immediate stop...
    "%PGSQL_DIR%\bin\pg_ctl.exe" -D "%DATA_DIR%" -m immediate stop
)

echo [stop-postgres] Local PostgreSQL server stopped successfully.
endlocal
exit /b 0
