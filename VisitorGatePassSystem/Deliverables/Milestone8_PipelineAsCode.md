# Milestone 8: Pipeline as Code

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Migrate from the freestyle Jenkins job to a **Pipeline-as-Code** approach using a `Jenkinsfile`. Implement a multi-stage pipeline that performs checkout, build, test, package, and deploy. Add parameterized configuration for flexible builds. This milestone completes **US-12**.

---

### 2. User Story Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-12 | As a **DevOps Engineer**, I want a Jenkins CI/CD pipeline so that every commit is automatically built and tested. | MEDIUM | ✅ DONE |

---

### 3. Jenkinsfile — Pipeline Definition

**Location:** `Jenkinsfile` (project root)

```groovy
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "visitor-gate-pass"
        DOCKER_REGISTRY = "your-dockerhub-username"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Unit Test') {
            steps {
                bat 'mvn clean compile test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }
        
        stage('Selenium UI Tests') {
            steps {
                bat 'mvn test -Dtest=GatePassUITest'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    bat "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    bat "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }

        stage('Deploy') {
            steps {
                bat 'docker-compose down'
                bat 'docker-compose up -d'
            }
        }
        
        stage('Health Check') {
            steps {
                sleep 15
                bat 'curl -f http://localhost:8080/actuator/health || exit 1'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

---

### 4. Pipeline Stages Explained

| Stage | Command | Purpose | Failure Behavior |
|-------|---------|---------|-----------------|
| Checkout | `checkout scm` | Clones source code from Git | Pipeline aborts |
| Build & Unit Test | `mvn clean compile test` | Compiles Java 17 source and runs unit tests | Pipeline fails, no deployment |
| Package | `mvn package -DskipTests` | Creates executable JAR in `target/` | Pipeline fails |
| Selenium UI Tests | `mvn test -Dtest=GatePassUITest` | Runs headless Chrome Selenium tests | Screenshots archived; pipeline fails |
| Docker Build | `docker build` + `docker tag` | Creates versioned Docker image | Pipeline fails |
| Deploy | `docker-compose up -d` | Deploys app + MySQL containers | Pipeline fails |
| Health Check | `curl /actuator/health` | Verifies application is running | Pipeline marked unstable |

**Stage Flow Diagram:**
```
Checkout → Build & Test → Package → Selenium → Docker Build → Deploy → Health Check
   │            │            │          │            │           │          │
   ▼            ▼            ▼          ▼            ▼           ▼          ▼
  SCM        Compile       JAR      UI Tests      Image      Compose    200 OK
  Clone      + Tests     Created    + Screenshots  Created   Started    Verified
```

---

### 5. Parameterized Configuration

The pipeline uses **environment variables** for flexible configuration:

| Variable | Default | Purpose |
|----------|---------|---------|
| `DOCKER_IMAGE` | `visitor-gate-pass` | Docker image name |
| `DOCKER_REGISTRY` | `your-dockerhub-username` | Docker Hub registry prefix |
| `DOCKER_TAG` | `${BUILD_NUMBER}` | Auto-incremented version tag |

**Application Properties (Overridable):**

| Variable | Default | Used In |
|----------|---------|---------|
| `DB_URL` | `jdbc:mysql://localhost:3306/gatepass_db...` | `application.properties` |
| `DB_USERNAME` | `root` | `application.properties` |
| `DB_PASSWORD` | `root` | `application.properties` |

These are overridden in `docker-compose.yml` for container deployment:
```yaml
environment:
  - DB_URL=jdbc:mysql://db:3306/gatepass_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
  - DB_USERNAME=root
  - DB_PASSWORD=rootpassword
```

---

### 6. Jenkins Pipeline Job Configuration

| Property | Value |
|----------|-------|
| Job Name | `visitor-gate-pass-pipeline` |
| Job Type | Pipeline |
| Pipeline Definition | Pipeline script from SCM |
| SCM | Git |
| Repository URL | `D:\devops\visitor-gate-pass` or GitHub HTTPS URL |
| Branch | `*/develop` |
| Script Path | `Jenkinsfile` |

**Setup Steps:**
```
1. Jenkins Dashboard → New Item → "visitor-gate-pass-pipeline" → Pipeline
2. Pipeline → Definition: "Pipeline script from SCM"
3. SCM: Git
   - Repository URL: (local path or GitHub URL)
   - Branch: */develop
   - Script Path: Jenkinsfile
4. Save → Build Now
```

---

### 7. Deployed Application Evidence

After successful pipeline execution:

| Check | Expected Result |
|-------|----------------|
| `http://localhost:8080/login` | Login page loads with glassmorphism UI |
| `http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| `docker ps` | Two containers running: `app` (port 8080), `db` (port 3306) |
| Jenkins Artifacts | `visitor-gate-pass-1.0.0-SNAPSHOT.jar` archived |

---

### 8. Pipeline vs. Freestyle Comparison

| Aspect | Freestyle Job (Milestone 7) | Pipeline (Milestone 8) |
|--------|---------------------------|----------------------|
| Configuration | Jenkins UI only | Jenkinsfile in Git |
| Version Control | Not versioned | Versioned with code |
| Stages | Single build step | Multi-stage pipeline |
| Flexibility | Limited | Full Groovy scripting |
| Portability | Tied to Jenkins instance | Portable across instances |
| Docker Integration | Manual | Built into pipeline |

---

### 9. Definition of Done — Checklist

- [x] **Jenkinsfile Created** — Multi-stage pipeline definition committed to repository.
- [x] **Pipeline Job Created** — Jenkins Pipeline job pointing to Jenkinsfile.
- [x] **All Stages Pass** — Checkout, Build, Test, Package, Docker Build, Deploy, Health Check.
- [x] **Artefacts Archived** — JAR file and screenshots stored in Jenkins.
- [x] **App Deployed** — Application accessible at `http://localhost:8080`.
- [x] **Health Check** — `/actuator/health` returns `{"status":"UP"}`.
- [x] **Parameterized Config** — Docker and DB credentials configurable via environment variables.
- [x] **Code Versioned** — Jenkinsfile tracked in Git alongside application code.
