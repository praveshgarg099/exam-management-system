#!/usr/bin/env bash
# ==============================================================================
# Script: build-app-jar.sh
# Purpose: Compiles all project sources, bundles images, and packages a standalone
#          production-grade application JAR with manifest and classpath entries.
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

echo "========================================================================"
echo "      BUILDING EXAM MANAGEMENT SYSTEM APPLICATION JAR                  "
echo "========================================================================"
cd "${PROJECT_ROOT}"

# 1. Ensure output directories exist
rm -rf target/staging dist
mkdir -p bin target/staging/images dist/lib dist/conf

# 2. Compile all source files using sources manifest
echo "[1/5] Compiling Java source files via @sources.txt..."
javac -cp "Resource/*:src" -d bin @sources.txt

# 3. Stage compiled classes
echo "[2/5] Staging compiled classes..."
cp -R bin/* target/staging/

# 4. Stage application images directly into classpath /images
echo "[3/5] Staging application images to classpath..."
if [ -d "images" ]; then
    cp -R images/* target/staging/images/
fi

# 5. Copy dependencies to dist/lib/
echo "[4/5] Copying dependencies and formatting MANIFEST.MF..."
for jar in Resource/*.jar; do
    if [ -f "$jar" ]; then
        cp "$jar" dist/lib/
    fi
done

# Write RFC-compliant MANIFEST.MF with 72-byte max line wrapping
python3 -c "
import os

jars = [os.path.basename(p) for p in sorted(os.listdir('Resource')) if p.endswith('.jar')]
cp_str = ' '.join('lib/' + j for j in jars)

with open('target/MANIFEST.MF', 'w', encoding='utf-8') as f:
    f.write('Manifest-Version: 1.0\r\n')
    f.write('Main-Class: exam_management_syatem.app.Main\r\n')
    f.write('Implementation-Title: Exam Management System\r\n')
    f.write('Implementation-Version: 1.0.0\r\n')
    
    header = 'Class-Path: '
    full_cp = header + cp_str
    
    lines = []
    while len(full_cp.encode('utf-8')) > 71:
        chunk = full_cp[:70]
        last_sp = chunk.rfind(' ')
        if last_sp > 15:
            lines.append(full_cp[:last_sp])
            full_cp = ' ' + full_cp[last_sp+1:]
        else:
            lines.append(full_cp[:70])
            full_cp = ' ' + full_cp[70:]
    lines.append(full_cp)
    
    for line in lines:
        f.write(line + '\r\n')
"

# 6. Package final application JAR
echo "[5/5] Creating dist/exam-management-system.jar..."
jar -cfm dist/exam-management-system.jar target/MANIFEST.MF -C target/staging .

# 7. Copy configuration template
cp packaging/common/conf/database.properties.template dist/conf/database.properties

echo "========================================================================"
echo "SUCCESS: Standalone application distribution packaged in dist/"
echo "Run with: java -jar dist/exam-management-system.jar"
echo "========================================================================"
