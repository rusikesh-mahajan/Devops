# Milestone 11: Docker Lifecycle

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Create a `Dockerfile` for the application, establishing a consistent containerized environment. Document the lifecycle commands for building, running, and managing the Docker container locally. This addresses the first part of **US-11**.

---

### 2. User Story Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-11 | As a **DevOps Engineer**, I want a Dockerized deployment so that the app runs consistently across environments. | MEDIUM | 🔄 In Progress (Dockerfile Created) |

---

### 3. Dockerfile Implementation

**Location:** `Dockerfile` (project root)

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/visitor-gate-pass-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Explanation:**
- `FROM eclipse-temurin:17-jdk-alpine`: Uses a lightweight Alpine Linux image with Java 17 installed.
- `VOLUME /tmp`: Creates a mount point for temporary files, which Tomcat (embedded in Spring Boot) requires.
- `ARG JAR_FILE=...`: Defines a build argument pointing to the compiled JAR file.
- `COPY ${JAR_FILE} app.jar`: Copies the JAR from the host machine into the container as `app.jar`.
- `EXPOSE 8080`: Documents that the container listens on port 8080.
- `ENTRYPOINT [...]`: Specifies the command to run the Spring Boot application when the container starts.

---

### 4. Container Lifecycle Documentation

The following commands detail the lifecycle of the Docker container during local development and testing.

#### 4.1 Build the Image
Before building the Docker image, the application must be packaged via Maven.
```bash
# Package the application
mvn clean package -DskipTests

# Build the Docker image
docker build -t visitor-gate-pass:local .
```

#### 4.2 Run the Container (Standalone)
If running a separate database is not required (e.g., using H2 in-memory db for testing).
```bash
docker run -d -p 8080:8080 --name gatepass-app visitor-gate-pass:local
```

#### 4.3 View Logs
```bash
docker logs gatepass-app
```

#### 4.4 Stop the Container
```bash
docker stop gatepass-app
```

#### 4.5 Remove the Container
```bash
docker rm gatepass-app
```

---

### 5. Multi-Container Orchestration (Docker Compose)

For a complete local setup with MySQL, `docker-compose.yml` is used.

**Location:** `docker-compose.yml`

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://db:3306/gatepass_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      - DB_USERNAME=root
      - DB_PASSWORD=rootpassword
    depends_on:
      - db
    networks:
      - app-network

  db:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=gatepass_db
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

**Lifecycle with Compose:**
```bash
# Start both app and db containers in detached mode
docker-compose up -d

# Stop and remove containers, networks, and volumes
docker-compose down
```

---

### 6. Definition of Done — Checklist

- [x] **Dockerfile Created** — Located at project root using a lightweight JDK image.
- [x] **Docker Compose Configured** — Orchestrates the Spring Boot app and MySQL database.
- [x] **Container Builds** — `docker build` completes successfully.
- [x] **Container Runs** — Application is accessible when running via `docker-compose up`.
- [x] **Lifecycle Documented** — Build, run, stop, and log commands are documented.
