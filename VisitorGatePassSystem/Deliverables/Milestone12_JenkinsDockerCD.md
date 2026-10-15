# Milestone 12: Jenkins-Docker CD

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Integrate Docker image creation and deployment directly into the Jenkins Continuous Delivery (CD) pipeline. Version the Docker image automatically and manage its lifecycle via the Jenkinsfile. This milestone completes **US-11** and finalizes **US-12**.

---

### 2. User Stories Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-11 | As a **DevOps Engineer**, I want a Dockerized deployment so that the app runs consistently across environments. | MEDIUM | ✅ DONE |
| US-12 | As a **DevOps Engineer**, I want a Jenkins CI/CD pipeline so that every commit is automatically built and tested. | MEDIUM | ✅ DONE |

---

### 3. Pipeline Implementation for CD

The `Jenkinsfile` orchestrates the Docker build and deployment processes.

#### 3.1 Environment Variables
Variables are defined at the start of the pipeline to manage image names and versioning dynamically based on the Jenkins build number.

```groovy
environment {
    DOCKER_IMAGE = "visitor-gate-pass"
    DOCKER_REGISTRY = "your-dockerhub-username" // Or private registry
    DOCKER_TAG = "${BUILD_NUMBER}"
}
```

#### 3.2 Docker Build Stage
This stage executes the `docker build` command and tags it with both the build number and the registry prefix.

```groovy
stage('Docker Build') {
    steps {
        script {
            bat "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
            bat "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
    }
}
```

#### 3.3 Docker Push Stage (Optional/Commented out for MVP)
In a full production environment, the image would be pushed to a registry.

```groovy
// stage('Docker Push') {
//     steps {
//         withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
//             bat "echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin"
//             bat "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"
//         }
//     }
// }
```

#### 3.4 Deploy Stage
The deploy stage uses `docker-compose` to bring up the full environment (App + DB) locally on the Jenkins agent. It tears down the old version first.

```groovy
stage('Deploy') {
    steps {
        // For MVP, run it locally using docker-compose
        bat 'docker-compose down'
        bat 'docker-compose up -d'
    }
}
```

---

### 4. Continuous Delivery Workflow

The complete CI/CD flow now looks like this:

1. **SCM Checkout:** Jenkins pulls the latest code.
2. **Build & Test:** Maven compiles the code and runs unit tests.
3. **Package:** Maven packages the application into a JAR file.
4. **UI Tests:** Selenium tests run against the packaged application.
5. **Docker Build:** A new Docker image is built using the generated JAR and tagged with the Jenkins build number (e.g., `visitor-gate-pass:15`).
6. **Deploy:** `docker-compose down` removes the old container, and `docker-compose up -d` starts the new version.
7. **Health Check:** Jenkins queries `/actuator/health` to verify the deployment was successful.

---

### 5. Automated Versioning Evidence

By utilizing `${BUILD_NUMBER}`, every execution of the pipeline results in a uniquely versioned Docker image.

**Example Docker Images after 3 pipeline runs:**
```bash
> docker images
REPOSITORY                           TAG       IMAGE ID       CREATED          SIZE
visitor-gate-pass                    3         a1b2c3d4e5f6   2 minutes ago    285MB
visitor-gate-pass                    2         b2c3d4e5f6a1   10 minutes ago   285MB
visitor-gate-pass                    1         c3d4e5f6a1b2   30 minutes ago   285MB
```

---

### 6. Definition of Done — Checklist

- [x] **Dynamic Tagging** — Jenkinsfile uses `${BUILD_NUMBER}` for Docker tags.
- [x] **Automated Build** — Pipeline automatically builds the Docker image.
- [x] **Automated Deploy** — Pipeline deploys the new image using Docker Compose.
- [x] **Zero Touch** — Committing code results in a deployed, updated container without manual intervention.
