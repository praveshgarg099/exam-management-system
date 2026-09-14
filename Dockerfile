# ==============================================================================
# Exam Management System - All-In-One Container Deployment
# Multi-stage build: compiles Java application from source and packages with
# PostgreSQL, virtual display, and noVNC web streaming server on port 8080.
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build & Package Application JAR
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy source manifests and libraries
COPY sources.txt /build/
COPY src/ /build/src/
COPY Resource/ /build/Resource/
COPY images/ /build/images/

# Create build output directories
RUN mkdir -p /build/bin /build/dist/lib

# Compile all classes via sources.txt
RUN javac -cp "Resource/*:src" -d bin @sources.txt

# Stage runtime dependencies
RUN cp Resource/*.jar /build/dist/lib/

# Copy images to classpath
RUN mkdir -p bin/images && cp -r images/* bin/images/ 2>/dev/null || true

# Generate manifest with RFC 822 compliant Class-Path
RUN python3 -c "\
import glob, os; \
jars = sorted([os.path.basename(p) for p in glob.glob('/build/dist/lib/*.jar')]); \
cp = ' '.join(['lib/' + j for j in jars]); \
lines = ['Manifest-Version: 1.0', 'Main-Class: exam_management_syatem.app.Main']; \
cur = 'Class-Path: '; \
for part in cp.split(' '): \
    if len(cur + ' ' + part) > 70: \
        lines.append(cur); cur = ' ' + part; \
    else: cur = cur + (' ' if cur != 'Class-Path: ' else '') + part; \
lines.append(cur); \
open('/build/MANIFEST.MF', 'w').write('\n'.join(lines) + '\n')"

# Create standalone application JAR
RUN jar -cfm /build/dist/exam-management-system.jar /build/MANIFEST.MF -C bin .

# ------------------------------------------------------------------------------
# Stage 2: Self-Contained Runtime (PostgreSQL + JRE + Web GUI)
# ------------------------------------------------------------------------------
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=UTC

# Install PostgreSQL, Java JRE, virtual display (Xvfb), window manager, and web streaming
RUN apt-get update && apt-get install -y --no-install-recommends \
    postgresql \
    postgresql-contrib \
    openjdk-21-jre-headless \
    xvfb \
    x11vnc \
    openbox \
    novnc \
    websockify \
    ca-certificates \
    curl \
    procps \
    net-tools \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built distribution from builder stage
COPY --from=builder /build/dist /app/dist

# Symlink noVNC main page so root URL (/) opens the application directly
RUN ln -sf /usr/share/novnc/vnc.html /usr/share/novnc/index.html

# Copy entrypoint script
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Container configuration
ENV DISPLAY=:1
ENV RESOLUTION=1280x800
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=exam_management
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

# Web interface port
EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
