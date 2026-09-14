@echo off
rem ==============================================================================
rem Exam Management System - Custom Modular Runtime Builder (Windows)
rem Uses jlink to produce a lightweight (~50MB) self-contained JRE for EMS.
rem ==============================================================================

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%..\..\.."
set "PROJECT_ROOT=%CD%"
popd

if "%~1"=="" (
    set "OUTPUT_DIR=%PROJECT_ROOT%\target\runtime"
) else (
    set "OUTPUT_DIR=%~1"
)

echo ============================================================
echo     Exam Management System - Custom Modular JRE Builder     
echo ============================================================
echo Project Root : %PROJECT_ROOT%
echo Output Dir   : %OUTPUT_DIR%

rem 1. Locate jlink
set "JLINK_BIN="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jlink.exe" (
        set "JLINK_BIN=%JAVA_HOME%\bin\jlink.exe"
    )
)

if not defined JLINK_BIN (
    for %%I in (jlink.exe) do (
        if not "%%~$PATH:I"=="" set "JLINK_BIN=%%~$PATH:I"
    )
)

if not defined JLINK_BIN (
    echo ERROR: 'jlink.exe' not found in PATH or JAVA_HOME. 1>&2
    exit /b 1
)

echo Using jlink  : %JLINK_BIN%

rem 2. Required modules
set "MODULES=java.base,java.desktop,java.sql,java.naming,java.management,java.logging,java.xml,java.security.jgss,java.security.sasl,java.transaction.xa,jdk.crypto.ec,jdk.unsupported"

echo Modules to include:
echo   %MODULES%

rem 3. Clean previous runtime
if exist "%OUTPUT_DIR%" (
    echo Cleaning existing runtime at %OUTPUT_DIR%...
    rmdir /s /q "%OUTPUT_DIR%"
)

rem 4. Run jlink
echo Invoking jlink...
"%JLINK_BIN%" --add-modules %MODULES% --strip-debug --no-man-pages --no-header-files --compress zip-6 --output "%OUTPUT_DIR%"
if errorlevel 1 (
    echo Retrying with legacy compress option...
    "%JLINK_BIN%" --add-modules %MODULES% --strip-debug --no-man-pages --no-header-files --compress=2 --output "%OUTPUT_DIR%"
    if errorlevel 1 (
        echo Retrying without compression...
        "%JLINK_BIN%" --add-modules %MODULES% --strip-debug --no-man-pages --no-header-files --output "%OUTPUT_DIR%"
        if errorlevel 1 (
            echo ERROR: Failed to create modular runtime with jlink. 1>&2
            exit /b 1
        )
    )
)

rem 5. Verify generated runtime
if exist "%OUTPUT_DIR%\bin\java.exe" (
    echo ------------------------------------------------------------
    echo Verifying generated runtime:
    "%OUTPUT_DIR%\bin\java.exe" -version
    echo ------------------------------------------------------------
    echo SUCCESS: Lightweight JRE created at: %OUTPUT_DIR%
) else (
    echo ERROR: Generated runtime binary not found at %OUTPUT_DIR%\bin\java.exe 1>&2
    exit /b 1
)

endlocal
