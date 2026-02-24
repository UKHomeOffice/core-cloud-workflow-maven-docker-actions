# ==============================================================================
# CCL-6306 | Dummy Service Dockerfile
# Multi-stage build – safe multi-arch + Checkov-friendly defaults
# ==============================================================================

# ── Stage 1: Build ─────────────────────────────────────────────────────────────
# Use official Maven image (includes Maven + Temurin JDK) to avoid apk installs
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy POM first – better layer caching on dependency downloads
COPY tests/dummy-service/pom.xml ./pom.xml
RUN mvn -B -ntp -f pom.xml dependency:go-offline

# Copy source and build
COPY tests/dummy-service/src/ ./src/
RUN mvn -B -ntp -f pom.xml clean package -DskipTests && \
    mv target/*.jar /build/app.jar

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime

# Non-root user and group (works on Debian/Ubuntu base)
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/app.jar /app/app.jar

LABEL org.opencontainers.image.source="https://github.com/UKHomeOffice/core-cloud-workflow-maven-docker-actions" \
      org.opencontainers.image.description="CCL-6306 dummy service – Core Cloud Platform" \
      org.opencontainers.image.licenses="MIT"

USER appuser

EXPOSE 8080

# Healthcheck requires curl (present in many images but not guaranteed).
# Install curl in a minimal way.
USER root
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
USER appuser

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD curl -fsS http://localhost:8080/api/status || exit 1

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]