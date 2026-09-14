@echo off
rem ==============================================================================
rem Exam Management System - Application Launcher (Windows)
rem Bootstraps database service, selects bundled JRE, and starts EMS desktop UI.
rem ==============================================================================

setlocal enabledelayedexpansion

set "APP_DIR=%~dp0"
cd /d "%APP_DIR%"

rem 1. Ensure PostgreSQL service is active
if exist "%APP_DIR%scripts\setup-local-postgres.bat" (
    call "%APP_DIR%scripts\setup-local-postgres.bat"
)

rem 2. Locate Java binary (prefer bundled modular runtime javaw.exe)
set "JAVA_EXE="
set "JAVAW_EXE="

if exist "%APP_DIR%runtime\bin\javaw.exe" (
    set "JAVAW_EXE=%APP_DIR%runtime\bin\javaw.exe"
    set "JAVA_EXE=%APP_DIR%runtime\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javaw.exe" (
        set "JAVAW_EXE=%JAVA_HOME%\bin\javaw.exe"
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    )
)

if not defined JAVAW_EXE (
    for %%I in (javaw.exe) do (
        if not "%%~$PATH:I"=="" set "JAVAW_EXE=%%~$PATH:I"
    )
    for %%I in (java.exe) do (
        if not "%%~$PATH:I"=="" set "JAVA_EXE=%%~$PATH:I"
    )
)

if not defined JAVAW_EXE (
    echo ERROR: No Java Runtime Environment found.
    echo Please reinstall Exam Management System or ensure Java 19+ is installed.
    pause
    exit /b 1
)

set "JAR_PATH=%APP_DIR%exam-management-system.jar"
if not exist "%JAR_PATH%" (
    echo ERROR: Application archive not found at "%JAR_PATH%".
    pause
    exit /b 1
)

rem 3. Launch application
rem If --debug or --console argument passed, run with console output
if /i "%~1"=="--debug" goto :run_debug
if /i "%~1"=="--console" goto :run_debug

start "" "%JAVAW_EXE%" -Dapp.home="%APP_DIR%" -Dfile.encoding=UTF-8 -jar "%JAR_PATH%"
exit /b 0

:run_debug
echo Starting Exam Management System in debug/console mode...
"%JAVA_EXE%" -Dapp.home="%APP_DIR%" -Dfile.encoding=UTF-8 -jar "%JAR_PATH%"
pause
exit /b %ERRORLEVEL%
