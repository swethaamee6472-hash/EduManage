@echo off
setlocal enabledelayedexpansion

:: Base directories
set "BASE_DIR=%~dp0"
set "SRC_DIR=%BASE_DIR%src"
set "BIN_DIR=%BASE_DIR%bin"
set "DATA_DIR=%BASE_DIR%data"

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"

echo ==========================================================
echo  EduManage - Student Data Management System
echo  Module 1: Authentication ^& Role Management (Swing)
echo ==========================================================
echo Compiling Java source files...

:: Find all Java files and enclose in quotes, replacing \ with /
if exist "%BASE_DIR%sources.txt" del "%BASE_DIR%sources.txt"
for /R "%SRC_DIR%" %%F in (*.java) do (
    set "FILE_PATH=%%F"
    set "FILE_PATH=!FILE_PATH:\=/!"
    echo "!FILE_PATH!" >> "%BASE_DIR%sources.txt"
)

:: Compile with Java compiler
javac -d "%BIN_DIR%" @"%BASE_DIR%sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    del "%BASE_DIR%sources.txt"
    exit /b %ERRORLEVEL%
)

del "%BASE_DIR%sources.txt"
echo [OK] Compilation completed successfully into bin\ directory.

if "%1"=="--test" (
    echo.
    echo Running automated verification test suite...
    java -cp "%BIN_DIR%" com.sset.erp.test.TestRunner
) else (
    echo Launching EduManage ERP GUI...
    echo.
    echo Demo Credentials for Testing:
    echo   • Administrator : admin / admin123
    echo   • Faculty       : faculty_cs / faculty123
    echo   • Student       : student_cs / student123
    echo   • Parent        : parent_cs / parent123
    echo ==========================================================
    java -cp "%BIN_DIR%" com.sset.erp.Main
)
