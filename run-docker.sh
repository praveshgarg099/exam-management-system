#!/usr/bin/env bash
# ==============================================================================
# Exam Management System - One-Click Docker Runner (macOS / Linux)
# ==============================================================================

set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: Docker is not installed or not in PATH." >&2
    echo "Please install Docker Desktop from https://www.docker.com/" >&2
    exit 1
fi

echo "============================================================"
echo "    Starting Exam Management System in Docker Container     "
echo "============================================================"

docker compose up -d --build

echo ""
echo "============================================================"
echo "SUCCESS: Container is running!"
echo "Open the Exam Management System in your browser at:"
echo ""
echo "    http://localhost:8080"
echo ""
echo "To stop the container, run: docker compose down"
echo "============================================================"
