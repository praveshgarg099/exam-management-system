#!/usr/bin/env bash
# ==============================================================================
# Exam Management System - Complete Cross-Platform Distribution Builder
# Assembles the application JAR, stages runtime dependencies, creates modular JRE,
# and executes regression verification against the packaged binaries.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "============================================================"
echo "    Exam Management System - Distribution Assembly          "
echo "============================================================"
echo "Project Root : ${PROJECT_ROOT}"

# 1. Build application JAR and stage lib/ & conf/
echo -e "\n[Step 1/3] Building application JAR..."
"${SCRIPT_DIR}/windows/scripts/build-app-jar.sh"

# 2. Build custom modular JRE runtime via jlink
echo -e "\n[Step 2/3] Building modular runtime..."
"${SCRIPT_DIR}/windows/scripts/build-runtime.sh" "${PROJECT_ROOT}/target/runtime"

# 3. Verify packaged distribution against verification test suite
echo -e "\n[Step 3/3] Running verification test suite using built artifacts..."
RUNTIME_JAVA="${PROJECT_ROOT}/target/runtime/bin/java"
JAR_PATH="${PROJECT_ROOT}/dist/exam-management-system.jar"
LIB_CP="${PROJECT_ROOT}/dist/lib/*"

"${RUNTIME_JAVA}" -cp "${JAR_PATH}:${LIB_CP}" exam_management_syatem.test.FreshDatabaseVerificationTest

echo -e "\n============================================================"
echo "SUCCESS: Distribution successfully assembled and verified!"
echo "Distribution directory : ${PROJECT_ROOT}/dist"
echo "Application JAR        : ${JAR_PATH} ($(du -sh "${JAR_PATH}" | cut -f1))"
echo "Modular JRE runtime    : ${PROJECT_ROOT}/target/runtime ($(du -sh "${PROJECT_ROOT}/target/runtime" | cut -f1))"
echo "============================================================"
