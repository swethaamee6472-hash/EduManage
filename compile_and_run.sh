#!/bin/bash
# ==============================================================================
# EduManage - Student Data Management System
# ==============================================================================

set -e

# Base directories
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
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

# Find all Java files and enclose in quotes for javac argfile compatibility
find "$SRC_DIR" -name "*.java" | sed 's/^/"/' | sed 's/$/"/' > "$BASE_DIR/sources.txt"

# Compile with Java compiler
javac -d "$BIN_DIR" @"$BASE_DIR/sources.txt"
rm -f "$BASE_DIR/sources.txt"

echo "✓ Compilation completed successfully into bin/ directory."

if [ "$1" == "--test" ]; then
    echo ""
    echo "Running automated verification test suite..."
    java -cp "$BIN_DIR" com.sset.erp.test.TestRunner
else
    echo "Launching EduManage ERP GUI..."
    echo ""
    echo "Demo Credentials for Testing:"
    echo "  • Administrator : admin / admin123"
    echo "  • Faculty       : faculty_cs / faculty123"
    echo "  • Student       : student_cs / student123"
    echo "  • Parent        : parent_cs / parent123"
    echo "=========================================================="
    java -cp "$BIN_DIR" com.sset.erp.Main
fi
