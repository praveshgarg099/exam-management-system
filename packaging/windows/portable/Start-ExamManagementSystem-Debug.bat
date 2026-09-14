@echo off
rem ==============================================================================
rem Exam Management System - Portable Launcher (Debug / Console Mode)
rem Runs the application with visible console output and pauses on exit
rem ==============================================================================

cd /d "%~dp0"

echo ==============================================================================
echo     Exam Management System - Debug Console Launcher
echo ==============================================================================
echo Directory: %CD%
echo.

if exist "runtime\bin\java.exe" (
    echo Using bundled runtime: runtime\bin\java.exe
    "runtime\bin\java.exe" -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
) else (
    echo Bundled runtime not found. Trying system java...
    java -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
)

echo.
echo Application exited with code %ERRORLEVEL%.
pause
