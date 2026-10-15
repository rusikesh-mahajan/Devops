# Automated Residential Visitor Gate-Pass System

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](http://localhost:8081/job/visitor-gate-pass-pipeline/)
[![Release](https://img.shields.io/badge/release-v1.0.0-blue.svg)](file:///d:/devops/visitor-gate-pass/)
[![Tests](https://img.shields.io/badge/tests-18%2F18%20passing-success.svg)](file:///d:/devops/visitor-gate-pass/reports/test_report.md)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)](file:///d:/devops/visitor-gate-pass/Dockerfile)
[![Ansible](https://img.shields.io/badge/Ansible-Provisioned-red.svg)](file:///d:/devops/visitor-gate-pass/ansible/)

An enterprise-grade, cloud-native full-stack web application and complete 15-week DevOps curriculum project for automated residential visitor management, featuring multi-role access control, finite state pass workflows, declarative CI/CD pipelines, automated testing, container orchestration, and configuration management.

---

## 1. System Architecture

```
+-----------------------------------------------------------------------------------+
|                                 CLIENT TIER                                       |
|    Modern Glassmorphic UI (Thymeleaf, Bootstrap 5.3, Live Stat Cards, Modals)     |
+------------------------------------------+----------------------------------------+
                                           | HTTP / HTTPS
                                           v
+-----------------------------------------------------------------------------------+
|                        SPRING BOOT 3 APPLICATION CORE                             |
|                                                                                   |
|  +------------------------------+  +-------------------------------------------+  |
|  |     Spring Security 6.2      |  |           WebController                   |  |
|  | - BCrypt Password Hashing    |  | - Finite State Machine Transitions        |  |
|  | - Role-Based Access Control  |  | - Resident Host Scoping                   |  |
|  | - CSRF Protection            |  | - Multi-Field Search & Filtering Logic    |  |
|  +------------------------------+  +-------------------------------------------+  |
|                                                  |                                |
|                                                  v                                |
|                           +-------------------------------------+                 |
|                           |    Spring Data JPA Repositories     |                 |
|                           | (Parameterized Custom JPQL Queries) |                 |
|                           +----------------------+--------------+                 |
+--------------------------------------------------|--------------------------------+
                                                   | JDBC / HikariCP Pool
                                                   v
+-----------------------------------------------------------------------------------+
|                              PERSISTENCE TIER                                     |
|                       MySQL 8.0 Relational Database                               |
|        (Health-Gated Startup, Persistent Data Volumes, Optimized Schema)          |
+-----------------------------------------------------------------------------------+
```

---

## 2. Core Features & Capabilities

- **Role-Based Access Control (RBAC):** Tailored experiences for `ROLE_ADMIN`, `ROLE_GUARD`, and `ROLE_RESIDENT`.
- **Visitor Pass State Machine:** Rigorous server-side transition lifecycle: `PENDING` -> `APPROVED` / `REJECTED` -> `CHECKED_IN` -> `CHECKED_OUT`.
- **Resident Pre-Registration:** Residents pre-approve guests with auto-generated alphanumeric pass codes (`GP-XXXX`).
- **Guard Walk-In Gate Operations:** Rapid walk-in registration, check-in, check-out, and pass slip view.
- **Glassmorphic UI & Analytics:** Live statistic counters (Total Passes, Active On-Site, Today's Check-ins, Pending Verification), status filter pills, and instant search.
- **Audit Details Modal:** Async JSON API (`/api/passes/{id}`) with timestamps for check-in and check-out.

---

## 3. Pre-Seeded User Accounts

| Username | Password | Role | Description & Flat Scope |
| :--- | :--- | :--- | :--- |
| `admin` | `password123` | `ROLE_ADMIN` | Full administrative access, views all flats |
| `guard1` | `password123` | `ROLE_GUARD` | Main gate guard, walk-in creation, check-in/out |
| `resident1` | `password123` | `ROLE_RESIDENT` | Resident of Flat 101, pre-registers visitors |
| `resident2` | `password123` | `ROLE_RESIDENT` | Resident of Flat 202, pre-registers visitors |

---

## 4. Technology Stack Specification

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Runtime & Language** | Java (JDK) | 21 LTS | Core programming language |
| **Web Framework** | Spring Boot | 3.2.4 | Application framework & Actuator |
| **Security** | Spring Security | 6.2 | Authentication, BCrypt, RBAC, CSRF |
| **Persistence** | Spring Data JPA / Hibernate | 3.2.4 | ORM and data access |
| **View Engine** | Thymeleaf | 3.1 | Server-side template rendering |
| **Database** | MySQL / H2 | 8.0 / In-memory | Relational database (prod & test) |
| **Containers** | Docker & Compose | 26.x / v2 | Multi-container encapsulation |
| **CI/CD** | Jenkins | 2.440+ | Declarative Pipeline-as-Code |
| **E2E Testing** | Selenium WebDriver | 4.19.1 | Headless Chrome user journey tests |
| **Config Management** | Ansible | Core | Automated provisioning & rollback |

---

## 5. Quickstart & Local Installation

### Prerequisites
- Java JDK 21 LTS installed and on `PATH`
- Apache Maven 3.9+
- Docker & Docker Compose v2 (optional for containerized deployment)

### Option A: Run via Docker Compose (Recommended)
```bash
# 1. Start application and MySQL with health gating
docker compose up -d

# 2. Verify containers are healthy
docker compose ps

# 3. Access web application
# Application: http://localhost:8080
# Actuator Health: http://localhost:8080/actuator/health
```
*(Note: Docker Compose maps MySQL to host port `3307` to prevent collisions with any existing host MySQL on port `3306`).*

### Option B: Run Locally via Maven
```bash
# 1. Clone repository
git clone https://github.com/<your-username>/visitor-gate-pass.git
cd visitor-gate-pass

# 2. Build and run unit tests
mvn clean test

# 3. Launch Spring Boot application (uses local MySQL or configured properties)
mvn spring-boot:run
```

---

## 6. Automated Testing & Verification

The project includes an 18-test automated suite with 100% pass rate:
- **JPA Repository Tests (4):** `VisitorPassRepositoryTest` validates entity persistence and multi-field custom search queries.
- **Web Controller & Security Tests (9):** `WebControllerTest` validates RBAC, route authorization, and state transitions using `MockMvc`.
- **Selenium Headless UI Tests (5):** `GatePassUITest` executes end-to-end browser journeys in headless Google Chrome.

### Running Tests:
```bash
# Run all unit and integration tests
mvn test

# Run UI tests specifically
mvn test -Dtest=GatePassUITest
```
Full-page journey screenshots are automatically saved to `screenshots/`:
- `screenshots/01_login_page.png`
- `screenshots/02_guard_dashboard.png`
- `screenshots/03_resident_dashboard.png`
- `screenshots/04_create_pass.png`
- `screenshots/05_pass_details_modal.png`

For full test metrics, refer to [reports/test_report.md](file:///d:/devops/visitor-gate-pass/reports/test_report.md).

---

## 7. Continuous Integration & Deployment (Jenkins)

The repository includes a production-ready declarative pipeline ([Jenkinsfile](file:///d:/devops/visitor-gate-pass/Jenkinsfile)):
- **Pipeline Architecture:** 9 stages including compile, test gate, Docker image assembly, compose deployment, and Actuator health check polling.
- **Jenkins Access:** Configured on port `8081` (`http://localhost:8081`).
- **Verified Build:** Build #7 completed with status **SUCCESS**.

---

## 8. Configuration Management & Reliability (Ansible)

Located in `ansible/`:
- `ansible/playbook.yml`: Declarative provisioning, environment templating, container orchestration, and health checks.
- `ansible/rollback.yml`: Automated rollback procedure to restore previous stable image upon health failure.
- `scripts/verify_provisioning.ps1`: Automated verification script confirming idempotency (`changed=0`) and container restart recovery.

---

## 9. 15-Week Curriculum Documentation Index

Complete technical documentation for all 15 weeks is organized in `docs/`:
- [Week 1: Problem Definition & Scope](file:///d:/devops/visitor-gate-pass/docs/01_problem_definition.md)
- [Week 2: Agile Planning & User Stories](file:///d:/devops/visitor-gate-pass/docs/02_agile_planning.md)
- [Week 2: Product Backlog](file:///d:/devops/visitor-gate-pass/docs/product_backlog.md)
- [Week 3: Requirements & Architecture](file:///d:/devops/visitor-gate-pass/docs/03_requirements_architecture.md)
- [Week 4: Git & GitHub Workflow](file:///d:/devops/visitor-gate-pass/docs/04_git_github_workflow.md)
- [Week 5: Feature Development & Security](file:///d:/devops/visitor-gate-pass/docs/05_feature_development.md)
- [Week 6: MVP Implementation](file:///d:/devops/visitor-gate-pass/docs/06_mvp_implementation.md)
- [Week 7: Jenkins CI Infrastructure](file:///d:/devops/visitor-gate-pass/docs/07_jenkins_ci.md)
- [Week 8: Pipeline Deployment as Code](file:///d:/devops/visitor-gate-pass/docs/08_pipeline_deployment.md)
- [Week 9: Selenium E2E UI Testing](file:///d:/devops/visitor-gate-pass/docs/09_selenium_testing.md)
- [Week 10: Continuous Automated Testing](file:///d:/devops/visitor-gate-pass/docs/10_continuous_testing.md)
- [Week 11: Docker Container Lifecycle](file:///d:/devops/visitor-gate-pass/docs/11_docker_lifecycle.md)
- [Week 12: Multi-Container Orchestration](file:///d:/devops/visitor-gate-pass/docs/12_docker_cd.md)
- [Week 13: Configuration Management](file:///d:/devops/visitor-gate-pass/docs/13_configuration_management.md)
- [Week 14: Provisioning Reliability & Rollback](file:///d:/devops/visitor-gate-pass/docs/14_provisioning_reliability.md)
- [Week 15: Final Release & System Defense](file:///d:/devops/visitor-gate-pass/docs/15_final_release_viva.md)

### Additional Capstone Artifacts:
- [Security Hardening Report](file:///d:/devops/visitor-gate-pass/reports/security_report.md)
- [Automated Testing Report](file:///d:/devops/visitor-gate-pass/reports/test_report.md)
- [Final Project Report](file:///d:/devops/visitor-gate-pass/reports/final_project_report.md)
- [Presentation Slide Deck](file:///d:/devops/visitor-gate-pass/presentation/project_presentation.md)
- [Comprehensive Viva Voce Q&A](file:///d:/devops/visitor-gate-pass/presentation/viva_qna.md)
- [Project Audit & Baseline](file:///d:/devops/visitor-gate-pass/PROJECT_AUDIT.md)
- [Curriculum Task Tracker](file:///d:/devops/visitor-gate-pass/TASK_TRACKER.md)

---

## 10. License & Academic Attribution
Developed as part of the 15-Week End-to-End DevOps Capstone Curriculum.  
Released under the MIT License.
