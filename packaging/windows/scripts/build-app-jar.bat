@echo off
rem ==============================================================================
rem Script: build-app-jar.bat
rem Purpose: Windows Batch script to compile and package standalone application JAR
rem ==============================================================================
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..\.."
cd /d "%PROJECT_ROOT%"

echo ========================================================================
echo       BUILDING EXAM MANAGEMENT SYSTEM APPLICATION JAR (WINDOWS)          
echo ========================================================================

rem 1. Clean output directories
if exist "target\staging" rmdir /s /q "target\staging"
if exist "dist" rmdir /s /q "dist"
mkdir "bin" 2>nul
mkdir "target\staging\images" 2>nul
mkdir "dist\lib" 2>nul
mkdir "dist\conf" 2>nul

rem 2. Compile sources
echo [1/5] Compiling Java source files via @sources.txt...
javac -cp "Resource/*;src" -d bin @sources.txt
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java compilation failed.
    exit /b %ERRORLEVEL%
)

rem 3. Stage classes
echo [2/5] Staging compiled classes...
xcopy /E /I /Y "bin\*" "target\staging\" >nul

rem 4. Stage images
echo [3/5] Staging application images to classpath...
if exist "images" (
    xcopy /E /I /Y "images\*" "target\staging\images\" >nul
)

rem 5. Build classpath and copy libs
echo [4/5] Copying dependencies and building manifest...
set "CP_LINE="
for %%F in (Resource\*.jar) do (
    copy /Y "%%F" "dist\lib\" >nul
    set "CP_LINE=!CP_LINE! lib/%%~nxF"
)

(
echo Manifest-Version: 1.0
echo Main-Class: exam_management_syatem.app.Main
echo Class-Path:!CP_LINE!
echo Created-By: Exam Management System Build System
echo Implementation-Title: Exam Management System
echo Implementation-Version: 1.0.0
) > target\MANIFEST.MF

rem 6. Package JAR
echo [5/5] Creating dist\exam-management-system.jar...
jar -cfm dist\exam-management-system.jar target\MANIFEST.MF -C target\staging .
if %ERRORLEVEL% neq 0 (
    echo [ERROR] JAR packaging failed.
    exit /b %ERRORLEVEL%
)

copy /Y "packaging\common\conf\database.properties.template" "dist\conf\database.properties" >nul

echo ========================================================================
echo SUCCESS: Standalone application distribution packaged in dist\
echo Run with: java -jar dist\exam-management-system.jar
echo ========================================================================
endlocal
