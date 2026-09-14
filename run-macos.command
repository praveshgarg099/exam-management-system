#!/bin/bash
# ==============================================================================
# Exam Management System - macOS Launcher
# Allows double-clicking from macOS Finder or executing from Terminal
# ==============================================================================

# Ensure working directory is the project directory
cd "$(dirname "$0")"

echo "============================================================"
echo "      Starting Exam Management System on macOS...           "
echo "============================================================"

# Check for Java runtime
JAVA_BIN=""
if [ -x "./target/runtime/bin/java" ]; then
    JAVA_BIN="./target/runtime/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="java"
else
    echo "[ERROR] Java runtime not found!"
    echo "Please install Java (JDK/JRE 19+) to run this application."
    read -p "Press Enter to exit..."
    exit 1
fi

JAR_PATH="./dist/exam-management-system.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "[ERROR] Application JAR not found at $JAR_PATH"
    echo "Please build the project first using: ./packaging/build-distribution.sh"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Using Java: $($JAVA_BIN -version 2>&1 | head -n 1)"
echo "Launching Exam Management System..."
echo "------------------------------------------------------------"

"$JAVA_BIN" -jar "$JAR_PATH"

EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo "------------------------------------------------------------"
    echo "[INFO] Application exited with code $EXIT_CODE."
    echo "If you encountered a database error, ensure PostgreSQL is running:"
    echo "  brew services start postgresql@14 (or your installed version)"
    echo "Or start via Docker:"
    echo "  ./run-docker.sh"
    read -p "Press Enter to exit..."
fi
