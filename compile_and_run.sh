#!/usr/bin/env bash
# EduManage - Student Data Management System
# Run with: ./compile_and_run.sh        (Mac/Linux)
# For Windows PowerShell use: .\compile_and_run.ps1

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$BASE_DIR/src"
BIN_DIR="$BASE_DIR/bin"
DATA_DIR="$BASE_DIR/data"

# Ensure Java compiler and runtime are located on macOS if standard stub fails
if ! javac -version >/dev/null 2>&1; then
    if [ -x "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/javac" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"
        export PATH="$JAVA_HOME/bin:$PATH"
    elif [ -x "/usr/libexec/java_home" ]; then
        JH=$(/usr/libexec/java_home 2>/dev/null)
        if [ -n "$JH" ] && [ -x "$JH/bin/javac" ]; then
            export JAVA_HOME="$JH"
            export PATH="$JAVA_HOME/bin:$PATH"
        fi
    fi
fi

mkdir -p "$BIN_DIR"
mkdir -p "$DATA_DIR"

echo "=========================================================="
echo " EduManage - Student Data Management System"
echo " Modules 1, 5-10: Authentication, Portals, Analytics, Notifications and Fees (Swing)"
echo "=========================================================="
echo "Compiling Java source files..."

# Build lib classpath (colon-separated on Mac/Linux)
LIB_CP=$(find "$BASE_DIR/lib" -name "*.jar" | tr '\n' ':')
LIB_CP="${LIB_CP%:}"  # strip trailing colon

# Collect all .java source files (quote paths to support spaces in directory names)
SOURCES_FILE="$BASE_DIR/sources.txt"
find "$SRC_DIR" -name "*.java" | sed 's/^/"/;s/$/"/' > "$SOURCES_FILE"

# Compile
javac -cp "$LIB_CP" -d "$BIN_DIR" "@$SOURCES_FILE"
COMPILE_EXIT=$?
rm -f "$SOURCES_FILE"

if [ $COMPILE_EXIT -ne 0 ]; then
    echo "[ERROR] Compilation failed."
    exit 1
fi

echo "[OK] Compilation completed successfully into bin/ directory."

# On macOS, build native AVFoundation Apple Silicon camera driver if swiftc is available
if [ "$(uname -s)" = "Darwin" ] && command -v swiftc >/dev/null 2>&1; then
    if [ ! -f "$BIN_DIR/mac_camera_stream" ] || [ "$SRC_DIR/native/mac_camera_stream.swift" -nt "$BIN_DIR/mac_camera_stream" ]; then
        echo "Building native macOS Apple Silicon camera driver..."
        swiftc -O "$SRC_DIR/native/mac_camera_stream.swift" -o "$BIN_DIR/mac_camera_stream" 2>/dev/null || true
    fi
fi

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
