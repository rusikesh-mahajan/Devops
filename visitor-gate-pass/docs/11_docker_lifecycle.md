# Week 11: Docker Image and Container Lifecycle

**Project:** Automated Residential Visitor Gate-Pass System  
**Document Code:** DOC-W11-DCL  
**Version:** 1.0.0  
**Date:** October 14, 2026  

---

## 1. Production Dockerfile Architecture

The application image is packaged using an optimized production `Dockerfile`:
```dockerfile
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevOps Team <devops@gatepass.local>"
LABEL description="Production image for Automated Residential Visitor Gate-Pass System"

WORKDIR /app

ARG JAR_FILE=target/visitor-gate-pass-1.0.0.jar
COPY ${JAR_FILE} app.jar

# Run as non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=25s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
```

### Security & Operational Hardening:
- **Base Image:** `eclipse-temurin:21-jre-alpine` (~160 MB footprint vs. >600 MB full JDK).
- **Non-Root Execution:** Creates and runs under `appuser` (UID/GID unprivileged).
- **Built-In Container Health Check:** Periodically polls `/actuator/health` via `wget`.
- **Entropy Performance:** Configured `-Djava.security.egd=file:/dev/./urandom` for fast non-blocking cryptographic key generation in container environments.

---

## 2. Docker Compose Orchestration & Health Gating

In `docker-compose.yml`, multi-container dependencies are handled without race conditions:
```yaml
services:
  app:
    build: .
    image: visitor-gate-pass:1.0.0
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://db:3306/gatepass_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      - DB_USERNAME=root
      - DB_PASSWORD=rootpassword
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure
    networks:
      - app-network

  db:
    image: mysql:8.0
    ports:
      - "3307:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=gatepass_db
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
      interval: 5s
      timeout: 5s
      retries: 12
      start_period: 15s
    networks:
      - app-network
    volumes:
      - db-data:/var/lib/mysql

networks:
  app-network:
    driver: bridge

volumes:
  db-data:
```

### Critical Resolution of MySQL Race Condition:
MySQL 8 requires 15-25 seconds on first boot to initialize storage directories and tables. Traditional `depends_on: [db]` only waits for container startup, causing Spring Boot to crash with `Communications link failure`.  
By adding `healthcheck` on `db` and `condition: service_healthy` on `app`, Docker Compose guarantees the Spring Boot application container starts **only after** MySQL is actively accepting TCP connections.

---

## 3. Container Lifecycle Commands & Verified Output

### Build & Tag:
```bash
docker build -t visitor-gate-pass:1.0.0 -t visitor-gate-pass:latest .
```

### Launch Containers:
```bash
docker compose -p visitor-gate-pass up -d
```
Output:
```text
Container visitor-gate-pass-db-1  Starting
Container visitor-gate-pass-db-1  Started
Container visitor-gate-pass-db-1  Waiting
Container visitor-gate-pass-db-1  Healthy
Container visitor-gate-pass-app-1 Starting
Container visitor-gate-pass-app-1 Started
```

### Inspect Container State:
```bash
docker compose -p visitor-gate-pass ps
```
Output:
```text
NAME                     IMAGE                    COMMAND               STATUS
visitor-gate-pass-app-1  visitor-gate-pass:1.0.0  "java -Djava.secur..."  Up (healthy)
visitor-gate-pass-db-1   mysql:8.0                "docker-entrypoint..."  Up (healthy)
```

### Verify Persistence Across Container Recreation:
```bash
docker compose -p visitor-gate-pass down
docker compose -p visitor-gate-pass up -d
```
All database records (users, passes, statuses) remain intact due to the named Docker volume `db-data`.
