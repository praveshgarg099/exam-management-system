#!/usr/bin/env bash
# ==============================================================================
# Exam Management System - Custom Modular Runtime Builder (macOS / Linux)
# Uses jlink to produce a lightweight (~50MB) self-contained JRE for EMS.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
OUTPUT_DIR="${1:-${PROJECT_ROOT}/target/runtime}"

echo "============================================================"
echo "    Exam Management System - Custom Modular JRE Builder     "
echo "============================================================"
echo "Project Root : ${PROJECT_ROOT}"
echo "Output Dir   : ${OUTPUT_DIR}"

# 1. Locate jlink
if command -v jlink >/dev/null 2>&1; then
    JLINK_BIN="$(command -v jlink)"
elif [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/jlink" ]]; then
    JLINK_BIN="${JAVA_HOME}/bin/jlink"
else
    echo "ERROR: 'jlink' not found in PATH or JAVA_HOME." >&2
    exit 1
fi

echo "Using jlink  : ${JLINK_BIN}"

# 2. Required modules (audited via jdeps for EMS + all runtime libraries)
MODULES="java.base,java.desktop,java.sql,java.naming,java.management,java.logging,java.xml,java.security.jgss,java.security.sasl,java.transaction.xa,jdk.crypto.ec,jdk.unsupported"

echo "Modules to include:"
echo "  ${MODULES}"

# 3. Determine jlink compression flag syntax
COMPRESS_FLAG="--compress=zip-6"
if "${JLINK_BIN}" --help | grep -q -- "--compress <compress>"; then
    COMPRESS_FLAG="--compress=zip-6"
elif "${JLINK_BIN}" --help | grep -q -- "--compress=<level>"; then
    COMPRESS_FLAG="--compress=2"
else
    COMPRESS_FLAG=""
fi

# 4. Clean previous runtime
if [[ -d "${OUTPUT_DIR}" ]]; then
    echo "Cleaning existing runtime at ${OUTPUT_DIR}..."
    rm -rf "${OUTPUT_DIR}"
fi
mkdir -p "$(dirname "${OUTPUT_DIR}")"

# 5. Build runtime
echo "Invoking jlink..."
BUILD_CMD=("${JLINK_BIN}" \
    --add-modules "${MODULES}" \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --output "${OUTPUT_DIR}")

if [[ -n "${COMPRESS_FLAG}" ]]; then
    BUILD_CMD+=("${COMPRESS_FLAG}")
fi

"${BUILD_CMD[@]}"

# 6. Verify generated runtime
if [[ -x "${OUTPUT_DIR}/bin/java" ]]; then
    echo "------------------------------------------------------------"
    echo "Verifying generated runtime:"
    "${OUTPUT_DIR}/bin/java" --version
    echo "Runtime size: $(du -sh "${OUTPUT_DIR}" | cut -f1)"
    echo "------------------------------------------------------------"
    echo "SUCCESS: Lightweight JRE created at: ${OUTPUT_DIR}"
else
    echo "ERROR: Generated runtime binary not found at ${OUTPUT_DIR}/bin/java" >&2
    exit 1
fi
