# Week 15: Final Release, Operational Verification, System Demonstration, and Viva Voce Guide

## 1. Overview and Milestone Completion
Week 15 represents the culmination of the 15-week DevOps curriculum for the **Automated Residential Visitor Gate-Pass System**. In this final phase, all software engineering, continuous integration, automated testing, containerization, and configuration management components are synthesized into an integrated, production-grade release tagged as `v1.0.0`.

---

## 2. 15-Week Curriculum Verification Checklist

| Week | Focus Area | Deliverables & Artifacts | Status |
| :---: | :--- | :--- | :---: |
| **01** | Problem Definition & Scope | [01_problem_definition.md](file:///d:/devops/visitor-gate-pass/docs/01_problem_definition.md) | Verified |
| **02** | Agile Planning & User Stories | [02_agile_planning.md](file:///d:/devops/visitor-gate-pass/docs/02_agile_planning.md), [product_backlog.md](file:///d:/devops/visitor-gate-pass/docs/product_backlog.md) | Verified |
| **03** | Architecture & Threat Modeling | [03_requirements_architecture.md](file:///d:/devops/visitor-gate-pass/docs/03_requirements_architecture.md) | Verified |
| **04** | Git & GitHub Branching Strategy | [04_git_github_workflow.md](file:///d:/devops/visitor-gate-pass/docs/04_git_github_workflow.md), Gitflow branches | Verified |
| **05** | Feature Development & Security | [05_feature_development.md](file:///d:/devops/visitor-gate-pass/docs/05_feature_development.md), Spring Security RBAC | Verified |
| **06** | MVP Implementation & Seeding | [06_mvp_implementation.md](file:///d:/devops/visitor-gate-pass/docs/06_mvp_implementation.md), DataInitializer | Verified |
| **07** | Jenkins CI Infrastructure Setup | [07_jenkins_ci.md](file:///d:/devops/visitor-gate-pass/docs/07_jenkins_ci.md), Jenkins service on port 8081 | Verified |
| **08** | Declarative Pipeline as Code | [08_pipeline_deployment.md](file:///d:/devops/visitor-gate-pass/docs/08_pipeline_deployment.md), [Jenkinsfile](file:///d:/devops/visitor-gate-pass/Jenkinsfile) | Verified |
| **09** | Selenium Automated UI Testing | [09_selenium_testing.md](file:///d:/devops/visitor-gate-pass/docs/09_selenium_testing.md), [GatePassUITest.java](file:///d:/devops/visitor-gate-pass/src/test/java/com/gatepass/GatePassUITest.java) | Verified |
| **10** | Continuous Automated Testing | [10_continuous_testing.md](file:///d:/devops/visitor-gate-pass/docs/10_continuous_testing.md), 18/18 Passing Tests | Verified |
| **11** | Docker Container Lifecycle | [11_docker_lifecycle.md](file:///d:/devops/visitor-gate-pass/docs/11_docker_lifecycle.md), [Dockerfile](file:///d:/devops/visitor-gate-pass/Dockerfile) (Alpine Java 21) | Verified |
| **12** | Multi-Container Orchestration | [12_docker_cd.md](file:///d:/devops/visitor-gate-pass/docs/12_docker_cd.md), [docker-compose.yml](file:///d:/devops/visitor-gate-pass/docker-compose.yml) (Health Gated) | Verified |
| **13** | Configuration Management (Ansible) | [13_configuration_management.md](file:///d:/devops/visitor-gate-pass/docs/13_configuration_management.md), [ansible/playbook.yml](file:///d:/devops/visitor-gate-pass/ansible/playbook.yml) | Verified |
| **14** | Provisioning Reliability & Rollback | [14_provisioning_reliability.md](file:///d:/devops/visitor-gate-pass/docs/14_provisioning_reliability.md), [ansible/rollback.yml](file:///d:/devops/visitor-gate-pass/ansible/rollback.yml) | Verified |
| **15** | Final Release & System Defense | [15_final_release_viva.md](file:///d:/devops/visitor-gate-pass/docs/15_final_release_viva.md), Reports & Presentation | Verified |

---

## 3. Release Management and Tagging
- **Semantic Release Version:** `v1.0.0`
- **Git Commit:** `392f02c` (and subsequent release commits on `main`)
- **Docker Image Tags:** `visitor-gate-pass:1.0.0`, `visitor-gate-pass:latest`
- **Release Changelog:**
  - Full State Machine implementation (`PENDING -> APPROVED/REJECTED -> CHECKED_IN -> CHECKED_OUT`).
  - Strict Role-Based Access Control (`ROLE_ADMIN`, `ROLE_GUARD`, `ROLE_RESIDENT`).
  - Resident Pre-Registration modal with auto-generated alphanumeric `GP-XXXX` pass codes.
  - Live statistical KPI cards, multi-field search, and instant status filtering.
  - Automated Docker Compose stack with MySQL 8.0 health gating.
  - Native Jenkins pipeline with automated compilation, unit testing, packaging, and deployment verification.

---

## 4. Live Operational Verification Status

The operational status of all services has been verified locally:
- **Application URL:** `http://localhost:8080/`
- **Health Check Endpoint:** `http://localhost:8080/actuator/health` (Returns HTTP 200 `{"status":"UP"}`)
- **Jenkins CI/CD Portal:** `http://localhost:8081/` (Active, Build #7 SUCCESS)
- **Container Network:** `visitor-gate-pass_default` linking `visitor-gate-pass-app-1` and `visitor-gate-pass-db-1`.

---

## 5. End-to-End System Demonstration Script

For live demonstration during evaluation or viva defense, follow this structured user walkthrough:

### Scenario 1: Resident Pre-Registers a Visitor
1. Navigate to `http://localhost:8080/login`.
2. Login as Resident (`username: resident1`, `password: password123`).
3. Note dashboard view: Restricted to passes hosted by Resident 1 (Flat 101).
4. Click **"Pre-Register Visitor"** button.
5. Enter visitor details:
   - Visitor Name: `David Miller`
   - Visitor Phone: `9876501234`
   - Purpose: `Family Dinner`
6. Submit form. Notice flash notification confirming creation with auto-generated code (e.g., `GP-7421`).
7. Pass status appears as `APPROVED` (pre-approved by host resident).

### Scenario 2: Guard Processes Arrival (Check-In)
1. Logout and login as Guard (`username: guard1`, `password: password123`).
2. Guard views all community passes across all flats.
3. Use the search bar to search `David Miller` or pass code `GP-7421`.
4. Click the **"Check-In"** button.
5. System records timestamp and updates status to `CHECKED_IN` (green badge).

### Scenario 3: Walk-In Visitor Registration & Guard Check-In
1. Guard clicks **"Register Walk-In Visitor"**.
2. Enter details:
   - Visitor Name: `Courier Service`
   - Visitor Phone: `9876543210`
   - Host Resident: `resident2` (Flat 202)
   - Purpose: `Package Delivery`
3. Pass is initially `PENDING`. Guard verifies and clicks **"Approve"**.
4. Guard clicks **"Check-In"** upon entry.

### Scenario 4: Visitor Departure (Check-Out)
1. In the passes table, locate `David Miller`.
2. Click **"Check-Out"**.
3. Status transitions to `CHECKED_OUT`. Action buttons are cleanly disabled.
4. Click **"Details"** to view the full modal audit trail with exact arrival and departure timestamps.

---

## 6. Viva Voce Defense Key Points
- **Architecture:** Monolithic Spring Boot 3 with layered separation (Entity, Repository, Controller, Service).
- **Security:** Spring Security filters enforce authentication, CSRF protection, BCrypt password hashing, and role authorization.
- **CI/CD Automation:** Jenkins pipeline executes declarative stages with test gating, ensuring zero regression releases.
- **Resilience:** Docker Compose healthcheck dependencies eliminate race conditions between database and web server.
- **Idempotency:** Ansible playbooks ensure repeated provisioning commands maintain target state without service interruptions.
