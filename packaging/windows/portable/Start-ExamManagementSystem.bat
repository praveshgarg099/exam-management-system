@echo off
rem ==============================================================================
rem Exam Management System - Portable Launcher
rem Starts the application directly using the bundled modular JRE runtime.
rem ==============================================================================

cd /d "%~dp0"

if exist "runtime\bin\javaw.exe" (
    start "" "runtime\bin\javaw.exe" -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
) else if exist "runtime\bin\java.exe" (
    start "" "runtime\bin\java.exe" -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
) else (
    start "" javaw -Dapp.home="%~dp0" -Dfile.encoding=UTF-8 -jar "exam-management-system.jar"
)

exit /b 0
