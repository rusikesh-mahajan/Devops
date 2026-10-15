# Milestone 2: Agile Planning and DevOps Workflow

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Product Backlog — User Stories and Acceptance Criteria

| ID | User Story | Priority | Acceptance Criteria |
|----|-----------|----------|-------------------|
| US-01 | As a **Guard**, I want to create a new visitor pass so that I can register visitors arriving at the gate. | HIGH | Guard can fill visitor name, phone, purpose, vehicle no, select host resident, and submit. Pass is saved with status "PENDING". |
| US-02 | As a **Resident**, I want to view pending visitor requests for my apartment so that I can approve or reject them. | HIGH | Resident sees only passes assigned to them. Can click "Approve" or "Reject". Status updates immediately. |
| US-03 | As a **Guard**, I want to check-in an approved visitor so that I can record their actual entry time. | HIGH | Guard sees "Check-In" button only for APPROVED passes. Clicking it sets status to "CHECKED_IN" and records timestamp. |
| US-04 | As a **Guard**, I want to check-out a visitor so that I can record their departure time. | HIGH | Guard sees "Check-Out" button only for CHECKED_IN passes. Clicking it sets status to "CHECKED_OUT" and records timestamp. |
| US-05 | As an **Admin**, I want to see a summary dashboard with total, pending, and active visitor counts. | HIGH | Dashboard shows three stat cards: Total Passes, Pending, Currently Checked In. |
| US-06 | As an **Admin**, I want to search visitor records by name, status, or date so that I can retrieve historical data. | HIGH | Search box filters records. Results update in the table below. |
| US-07 | As a **User**, I want to log in with my credentials so that I can access role-specific features. | HIGH | Login form accepts username/password. Redirects to dashboard on success. Shows error on failure. |
| US-08 | As a **User**, I want to log out securely so that my session is terminated. | MEDIUM | Logout button ends session and redirects to login page with confirmation. |
| US-09 | As a **Guard**, I want to see all visitor records in a table so that I can track current visitors. | MEDIUM | Table shows ID, Visitor Name, Phone, Host, Status, and action buttons. |
| US-10 | As an **Admin**, I want all data to persist in a MySQL database so that records survive restarts. | MEDIUM | Application connects to MySQL. Schema auto-created by Hibernate. |
| US-11 | As a **DevOps Engineer**, I want a Dockerized deployment so that the app runs consistently across environments. | MEDIUM | `docker-compose up` starts app + DB. App accessible on port 8080. |
| US-12 | As a **DevOps Engineer**, I want a Jenkins CI/CD pipeline so that every commit is automatically built and tested. | MEDIUM | Jenkinsfile defines checkout → build → test → package → deploy stages. |
| US-13 | As a **DevOps Engineer**, I want Selenium tests in the pipeline so that UI regressions are caught before deployment. | LOW | Selenium tests run in Jenkins. Failed tests stop deployment. |
| US-14 | As a **DevOps Engineer**, I want Ansible to configure the server so that provisioning is automated and repeatable. | LOW | Ansible playbook installs dependencies, deploys app, and is idempotent. |
| US-15 | As a **DevOps Engineer**, I want a health check endpoint so that deployment verification is automated. | LOW | `/actuator/health` returns 200 OK when the app is running. |

---

### 2. 15-Week Sprint Plan (Kanban/Scrum)

| Week | Sprint Goal | User Stories | Key Deliverables |
|------|------------|-------------|-----------------|
| 1 | Problem Definition | — | Problem statement, stakeholder list, MVP scope |
| 2 | Agile Planning | — | Backlog, sprint plan, DoD, DevOps diagram |
| 3 | Architecture & Setup | US-10 | SRS, architecture diagram, data model, local setup |
| 4 | Git Initialization | — | GitHub repo, README, .gitignore, branch policy |
| 5 | Feature Branch 1 | US-01, US-07 | Visitor pass creation, login, feature branch + PR |
| 6 | MVP Completion | US-02–US-06, US-08–US-09 | Full CRUD, dashboard, search, merge conflict, tag |
| 7 | Jenkins CI Setup | US-12 | Jenkins job, build trigger, archived artefact |
| 8 | Pipeline as Code | US-12 | Jenkinsfile, deployed app, parameterized config |
| 9 | Selenium Tests | US-13 | Test plan, 5 Selenium scripts, local report |
| 10 | CI Testing | US-13 | Jenkins test report, defect fix, rerun evidence |
| 11 | Docker Lifecycle | US-11 | Dockerfile, container lifecycle documentation |
| 12 | Jenkins-Docker CD | US-11, US-12 | Versioned image, registry push, auto-deploy |
| 13 | Ansible Config | US-14 | Playbook, roles, first execution log |
| 14 | Provisioning | US-14, US-15 | Idempotency, health check, rollback demo |
| 15 | Final Release | All | End-to-end demo, documentation, presentation |

---

### 3. Definition of Done (DoD)

A user story or task is considered **Done** when ALL of the following are satisfied:

1. **Code Complete** — Feature is implemented and compiles without errors.
2. **Tests Pass** — All existing unit and Selenium tests pass (`mvn clean test` = BUILD SUCCESS).
3. **Code Reviewed** — Changes have been reviewed via a Pull Request before merging.
4. **Committed** — Code is committed to the appropriate branch with a meaningful commit message.
5. **Documentation Updated** — README, API docs, or relevant deliverable documents are updated.
6. **Pipeline Green** — Jenkins pipeline passes all stages (build, test, package).
7. **Deployed** — The feature is deployable via `docker-compose` or direct `mvn spring-boot:run`.
8. **Acceptance Criteria Met** — All acceptance criteria for the user story are verified.

---

### 4. DevOps Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DevOps Lifecycle — Gate-Pass System              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐     │
│   │  PLAN    │───▶│ DEVELOP  │───▶│  BUILD   │───▶│  TEST    │     │
│   │          │    │          │    │          │    │          │     │
│   │ Backlog  │    │ Feature  │    │  Maven   │    │ Selenium │     │
│   │ Sprints  │    │ Branches │    │ Compile  │    │ JUnit    │     │
│   │ Stories  │    │ Git/PR   │    │ Package  │    │ Reports  │     │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘     │
│        ▲                                               │            │
│        │                                               ▼            │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐     │
│   │ MONITOR  │◀───│ OPERATE  │◀───│ DEPLOY   │◀───│ RELEASE  │     │
│   │          │    │          │    │          │    │          │     │
│   │ Health   │    │ Ansible  │    │ Docker   │    │ Jenkins  │     │
│   │ Checks   │    │ Config   │    │ Compose  │    │ Pipeline │     │
│   │ Logs     │    │ Playbook │    │ Registry │    │ Artifact │     │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘     │
│                                                                     │
│   ───────────── Continuous Feedback Loop ─────────────              │
└─────────────────────────────────────────────────────────────────────┘
```

**Toolchain Mapping:**

| Phase | Tool | Purpose |
|-------|------|---------|
| Plan | Jira / GitHub Issues | Backlog management, sprint tracking |
| Develop | Git + GitHub | Version control, branching, pull requests |
| Build | Maven | Compile, dependency management, packaging |
| Test | Selenium + JUnit 5 | Automated UI and integration testing |
| Release | Jenkins | CI/CD pipeline, artefact archival |
| Deploy | Docker + Docker Compose | Containerized deployment |
| Operate | Ansible | Configuration management, provisioning |
| Monitor | Spring Boot Actuator | Health checks, application metrics |
