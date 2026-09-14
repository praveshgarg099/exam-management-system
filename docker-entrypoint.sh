#!/usr/bin/env bash
# ==============================================================================
# Exam Management System - Container Entrypoint
# Initializes internal PostgreSQL, virtual display (Xvfb), window manager (Openbox),
# and noVNC web streaming server on port 8080, then launches the Swing application.
# ==============================================================================

set -eo pipefail

echo "============================================================"
echo "    Exam Management System - Container Launch               "
echo "============================================================"

# Default environment configuration
export DISPLAY="${DISPLAY:-:1}"
export RESOLUTION="${RESOLUTION:-1280x800}"
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-exam_management}"
export DB_USER="${DB_USER:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres}"

# 1. Start Internal PostgreSQL Service
echo "[1/4] Bootstrapping PostgreSQL database service..."
service postgresql start

# Ensure password and database exist
su - postgres -c "psql -c \"ALTER USER postgres WITH PASSWORD '${DB_PASSWORD}';\"" >/dev/null 2>&1 || true
su - postgres -c "psql -tc \"SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'\" | grep -q 1 || psql -c \"CREATE DATABASE ${DB_NAME};\"" >/dev/null 2>&1 || true
echo "      PostgreSQL is active on port 5432 (Database: ${DB_NAME})."

# 2. Start Virtual Display (Xvfb)
echo "[2/4] Initializing virtual display on ${DISPLAY} (${RESOLUTION}x24)..."
rm -f /tmp/.X1-lock /tmp/.X11-unix/X1 >/dev/null 2>&1 || true
Xvfb "${DISPLAY}" -screen 0 "${RESOLUTION}x24" &
sleep 1

# 3. Start Window Manager & noVNC Web Gateway
echo "[3/4] Starting Openbox window manager and web GUI gateway on port 8080..."
openbox &
x11vnc -display "${DISPLAY}" -nopw -forever -shared -bg -quiet

# Start websockify serving noVNC web interface on port 8080
websockify --web /usr/share/novnc 8080 localhost:5900 >/dev/null 2>&1 &
sleep 1

echo "[4/4] Launching Exam Management System..."
echo "============================================================"
echo "  APPLICATION IS LIVE!"
echo "  Access the Exam Management System in any web browser at:"
echo ""
echo "      http://localhost:8080"
echo ""
echo "============================================================"

# 4. Launch the Java Swing Application in Foreground
exec java -Dfile.encoding=UTF-8 \
    -Dapp.home="/app" \
    -cp "/app/dist/exam-management-system.jar:/app/dist/lib/*" \
    exam_management_syatem.app.Main
