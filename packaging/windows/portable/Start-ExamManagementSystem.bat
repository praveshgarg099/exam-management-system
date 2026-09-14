@echo off
rem ==============================================================================
rem Exam Management System - Portable Launcher
rem Starts the application directly using the bundled modular JRE runtime.
rem ==============================================================================

cd /d "%~dp0"

rem 1. Check if user attempted to run directly from inside a ZIP without extracting
if not exist "lib" (
    echo ==============================================================================
    echo [ERROR] Application dependencies folder "lib" was not found!
    echo.
    echo Please make sure you EXTRACT the entire ZIP file before running:
    echo   1. Right-click "ExamManagementSystem-Portable-Windows.zip"
    echo   2. Click "Extract All..."
    echo   3. Open the extracted folder and double-click "Start-ExamManagementSystem.bat"
    echo ==============================================================================
    pause
    exit /b 1
)

rem 2. Launch using bundled modular runtime or system fallback
if exist "runtime\bin\javaw.exe" (
    start "" "runtime\bin\javaw.exe" -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
) else if exist "runtime\bin\java.exe" (
    start "" "runtime\bin\java.exe" -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
) else (
    start "" javaw -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
)

exit /b 0
