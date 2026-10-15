# Milestone 14: Provisioning & Health Checks

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Finalize the automated provisioning workflow by validating the Ansible setup's idempotency and ensuring automated deployment verification through health checks. This milestone completes **US-14** and **US-15**.

---

### 2. User Stories Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-14 | As a **DevOps Engineer**, I want Ansible to configure the server so that provisioning is automated and repeatable. | LOW | ✅ DONE |
| US-15 | As a **DevOps Engineer**, I want a health check endpoint so that deployment verification is automated. | LOW | ✅ DONE |

---

### 3. Automated Health Check Implementation

#### 3.1 Application Endpoint (Spring Boot Actuator)

Spring Boot Actuator is included in `pom.xml` to expose operational information about the running application.

**Configuration (`application.properties`):**
```properties
# Expose Actuator health endpoint for CI/CD checks
management.endpoints.web.exposure.include=health
```

**Endpoint Behavior:**
When the application starts successfully and connects to the database, a `GET` request to `/actuator/health` returns HTTP 200 OK with the following JSON payload:
```json
{"status":"UP"}
```

#### 3.2 Jenkins Pipeline Verification

The `Jenkinsfile` utilizes this endpoint in the final stage to verify the deployment was successful before marking the pipeline as SUCCESS.

```groovy
stage('Health Check') {
    steps {
        sleep 15 // Wait for application startup
        // Simple health check using curl. Fails pipeline if non-200 response.
        bat 'curl -f http://localhost:8080/actuator/health || exit 1'
    }
}
```

---

### 4. Idempotency Demonstration

To prove the Ansible playbook's idempotency, it was executed twice sequentially.

**Run 1 (Initial Setup):**
```
PLAY RECAP *********************************************************************
localhost                  : ok=5    changed=3    unreachable=0    failed=0
```
*Result:* Three tasks reported `changed` (Directory created, file templated, docker-compose executed).

**Run 2 (Immediate Rerun):**
```
PLAY RECAP *********************************************************************
localhost                  : ok=5    changed=1    unreachable=0    failed=0
```
*Result:* Only the `shell` command for docker-compose reported `changed` (as `shell` commands inherently do unless specifically conditioned), but docker-compose itself recognized the containers were already running and took no action. Directory and file creation tasks reported `ok` (unchanged).

---

### 5. Rollback Demo Strategy

In the event that the health check fails, a rollback strategy is critical.

**Current Rollback Capability:**
Because Docker images are tagged with the Jenkins `${BUILD_NUMBER}`, rolling back is as simple as running `docker-compose` with the previous image tag.

**Simulated Rollback Process:**
1. Deployment of build `#15` fails the health check (e.g., database connection issue).
2. Engineer manually edits `docker-compose.yml` or the environment variables to point to image tag `:14`.
3. Engineer runs `ansible-playbook playbook.yml` (or Jenkins reruns the pipeline for an older commit).
4. Docker Compose detects the image change, stops `#15`, and starts `#14`.
5. Health check passes, service restored.

---

### 6. Definition of Done — Checklist

- [x] **Actuator Configured** — Spring Boot Actuator added and health endpoint exposed.
- [x] **Pipeline Health Check** — Jenkins checks `/actuator/health` post-deployment.
- [x] **Idempotency Validated** — Ansible playbook rerun confirms no side effects.
- [x] **Rollback Strategy Defined** — Rollback process using Docker tags documented.
