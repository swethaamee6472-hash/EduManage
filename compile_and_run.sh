#!/usr/bin/env bash
# EduManage - Student Data Management System
# Run with: ./compile_and_run.sh        (Mac/Linux)
# For Windows PowerShell use: .\compile_and_run.ps1

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$BASE_DIR/src"
BIN_DIR="$BASE_DIR/bin"
DATA_DIR="$BASE_DIR/data"

mkdir -p "$BIN_DIR"
mkdir -p "$DATA_DIR"

echo "=========================================================="
echo " EduManage - Student Data Management System"
echo " Module 1: Authentication & Role Management (Swing)"
echo "=========================================================="
echo "Compiling Java source files..."

# Build lib classpath (colon-separated on Mac/Linux)
LIB_CP=$(find "$BASE_DIR/lib" -name "*.jar" | tr '\n' ':')
LIB_CP="${LIB_CP%:}"  # strip trailing colon

# Collect all .java source files
SOURCES_FILE="$BASE_DIR/sources.txt"
find "$SRC_DIR" -name "*.java" > "$SOURCES_FILE"

# Compile
javac -cp "$LIB_CP" -d "$BIN_DIR" "@$SOURCES_FILE"
COMPILE_EXIT=$?
rm -f "$SOURCES_FILE"

if [ $COMPILE_EXIT -ne 0 ]; then
    echo "[ERROR] Compilation failed."
    exit 1
fi

echo "[OK] Compilation completed successfully into bin/ directory."

if [ "$1" = "--test" ]; then
    echo ""
    echo "Running automated verification test suite..."
    java -cp "$BIN_DIR:$LIB_CP" com.sset.erp.test.TestRunner
else
    echo "Launching EduManage ERP GUI..."
    echo ""
    echo "Demo Credentials for Testing:"
    echo "  * Administrator : admin / admin123"
    echo "  * Faculty       : faculty_cs / faculty123"
    echo "  * Student       : student_cs / student123"
    echo "  * Parent        : parent_cs / parent123"
    echo "=========================================================="
    java -cp "$BIN_DIR:$LIB_CP" com.sset.erp.Main
fi
