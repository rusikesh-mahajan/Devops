# Automated Residential Visitor Gate-Pass System: End-to-End DevOps Capstone Project Report

**Project Title:** Automated Residential Visitor Gate-Pass System  
**Academic Level:** College-Level / Capstone DevOps Project  
**Technology Stack:** Java 21 LTS, Spring Boot 3.2.4, Spring Security, Thymeleaf, MySQL 8.0, Docker & Docker Compose, Jenkins CI/CD, Selenium WebDriver, Ansible  
**Version:** `v1.0.0` (Production Release)  

---

## 1. Executive Summary and Abstract
The **Automated Residential Visitor Gate-Pass System** is an enterprise-grade, cloud-native web application designed to modernize, secure, and streamline visitor management in gated residential communities. Traditional paper-based entry logs suffer from data inaccuracies, security vulnerabilities, lack of auditability, and inefficient check-in workflows. 

This capstone project not only engineers a full-stack solution featuring multi-role access control (Admin, Security Guard, Resident) and finite-state pass management, but also implements a comprehensive, 15-week DevOps curriculum encompassing Gitflow source control, continuous integration via declarative Jenkins pipelines, automated unit and headless browser testing, multi-container orchestration with Docker Compose, and automated provisioning and rollback using Ansible.

All system components have been implemented, tested (18/18 tests passing), containerized, and verified locally with zero regressions.

---

## 2. Problem Definition and Project Motivation
Residential apartment complexes and gated societies encounter significant operational challenges with legacy manual visitor tracking:
- **Physical Log Vulnerabilities:** Handwritten registers are easily illegible, susceptible to unauthorized inspection, and vulnerable to loss or tampering.
- **Resident Inconvenience:** Residents lack visibility over pending visitors and cannot pre-authorize expected guests, forcing guards to make disruptive phone calls.
- **Audit Deficits:** Security personnel and resident welfare associations (RWAs) cannot rapidly search or audit historical entries during security incidents.
- **Deployment & Operational Fragility:** Lack of standardized DevOps automation often results in deployment drift, database connection race conditions, and lack of automated recovery.

The objective of this project was to construct both the application software and the automated deployment infrastructure required to solve these challenges.

---

## 3. System Architecture and Design

```
+---------------------------------------------------------------------------------+
|                                USER INTERFACE                                   |
|   Bootstrap 5.3 + Custom Glassmorphic CSS + Thymeleaf Templating Engine         |
|   Live KPI Stat Cards | Filter Pills | Multi-Field Search | Modal Dialogs       |
+---------------------------------------+-----------------------------------------+
                                        | HTTP / HTTPS
                                        v
+---------------------------------------------------------------------------------+
|                          SPRING BOOT 3 APPLICATION TIER                         |
|                                                                                 |
|  +-------------------------+  +--------------------------+  +----------------+  |
|  |     Spring Security     |  |      Web Controller      |  | Spring Actuator|  |
|  | (BCrypt, RBAC, Filters) |  |   (Pass State Machine)   |  | (/health,/info)|  |
|  +-------------------------+  +--------------------------+  +----------------+  |
|                                       |                                         |
|                                       v                                         |
|                       +--------------------------------+                        |
|                       |  Spring Data JPA Repositories  |                        |
|                       |   (Derived Queries & Search)   |                        |
|                       +---------------+----------------+                        |
+---------------------------------------|-----------------------------------------+
                                        | JDBC / HikariCP Pool
                                        v
+---------------------------------------------------------------------------------+
|                                PERSISTENCE TIER                                 |
|                         MySQL 8.0 Relational Database                           |
|       (Tables: visitor_passes, users; Constraints, Timestamps, Indices)         |
+---------------------------------------------------------------------------------+
```

### 3.1 Visitor Pass Finite State Machine (FSM)
Visitor access passes adhere to a strict, non-reversible state machine enforced server-side:

```
               [Resident Pre-Registers]
                         |
                         v
                    +----------+
                    | APPROVED |
                    +----+-----+
                         |
 [Guard Walk-In]         |
        |                |
        v                |
   +---------+           |
   | PENDING |-----------+
   +----+----+           | (Guard Check-In)
        |                |
        v                v
   +----------+    +------------+
   | REJECTED |    | CHECKED_IN |
   +----------+    +-----+------+
                         |
                         v (Guard Check-Out)
                   +-------------+
                   | CHECKED_OUT |
                   +-------------+
```

---

## 4. Technology Stack Specification

| Component | Technology | Version | Rationale |
| :--- | :--- | :--- | :--- |
| **Language** | Java (JDK) | 21 LTS | Long-term support, modern language features (records, pattern matching, virtual threads). |
| **Framework** | Spring Boot | 3.2.4 | Rapid application assembly, production-ready actuator, dependency injection. |
| **Security** | Spring Security | 6.2 | Declarative role-based authorization, BCrypt password encryption, CSRF protection. |
| **View Template** | Thymeleaf | 3.1 | Server-side HTML rendering with full model binding and automated escaping. |
| **Database** | MySQL | 8.0 | ACID-compliant relational persistence with containerized health check support. |
| **Container Engine**| Docker & Compose| 26.x / v2 | Declarative multi-container encapsulation and isolated bridge networking. |
| **CI/CD** | Jenkins | 2.440+ | Declarative Pipeline-as-Code (`Jenkinsfile`) with automated test gates. |
| **E2E Testing** | Selenium WebDriver| 4.19.1 | Automated headless Chrome user journey verification and screenshot capture. |
| **Config Mgmt** | Ansible | Core | Declarative server provisioning, idempotency verification, and rollback automation. |

---

## 5. 15-Week DevOps Implementation Lifecycle

### Weeks 1–3: Conception, Agile Planning & Architecture
- Formulated Problem Definition ([01_problem_definition.md](file:///d:/devops/visitor-gate-pass/docs/01_problem_definition.md)) and Product Backlog ([product_backlog.md](file:///d:/devops/visitor-gate-pass/docs/product_backlog.md)).
- Drafted user stories with acceptance criteria across Admin, Guard, and Resident personas.
- Modeled architecture and STRIDE security threats ([03_requirements_architecture.md](file:///d:/devops/visitor-gate-pass/docs/03_requirements_architecture.md)).

### Weeks 4–6: Source Control, Core Features & MVP
- Configured Gitflow workflow with `main`, `develop`, and `feature/*` conventions.
- Built Spring Data JPA persistence model and repository query methods.
- Implemented multi-role Spring Security with `DataInitializer` pre-seeding credentials (`admin`, `guard1`, `resident1`, `resident2`).
- Constructed responsive Thymeleaf UI with live statistical counters, filter pills, and modal dialogs.

### Weeks 7–8: Jenkins Continuous Integration & Pipelines
- Configured standalone Jenkins service on port 8081.
- Authored declarative `Jenkinsfile` featuring 9 distinct stages:
  1. Environment Preparation
  2. Compilation & Unit Tests
  3. Packaging & Test Gating
  4. Docker Image Assembly
  5. Container Deployment (`docker compose up -d`)
  6. Health Gating & Smoke Verification (`/actuator/health`)
  7. Optional Selenium UI Test Execution
  8. Artifact Archival
  9. Pipeline Teardown & Notification
- Verified successful automated execution (Jenkins Build #7: `SUCCESS`).

### Weeks 9–10: Automated Testing & Continuous Verification
- Developed 4 JPA repository tests, 9 Spring Security controller tests, and 5 Selenium browser tests.
- Successfully verified 18/18 tests passing (100% pass rate).
- Automated capture of 5 high-resolution journey screenshots in `screenshots/`.

### Weeks 11–12: Containerization & Compose Orchestration
- Crafted optimized multi-stage `Dockerfile` based on Alpine Linux with Java 21 JRE.
- Hardened container by provisioning and enforcing unprivileged user `appuser` (UID 1001).
- Solved database startup race conditions in `docker-compose.yml` via MySQL health checks and `condition: service_healthy`.

### Weeks 13–15: Provisioning, Reliability & Final Release
- Implemented Ansible playbooks (`ansible/playbook.yml`, `ansible/rollback.yml`).
- Validated idempotency (`changed=0`) and container self-healing resilience (`scripts/verify_provisioning.ps1`).
- Tagged production release `v1.0.0` on Git repository.

---

## 6. Testing, Quality Assurance, and Security Results

### 6.1 Automated Test Execution Summary
- **Total Tests:** 18
- **Pass Rate:** 100% (18 passed, 0 failed, 0 skipped)
- **Unit & Integration Duration:** 1.84 seconds
- **UI Test Duration:** 6.12 seconds

### 6.2 Security Posture Summary
- **Authentication:** All user passwords hashed via BCrypt. Zero plain-text credentials stored.
- **Authorization:** Resident views strictly scoped to flat ownership. Unauthenticated access blocked.
- **Container Hardening:** Containers execute as non-root user `appuser`.
- **Injection Defenses:** Prepared statements for SQL, automatic entity escaping for XSS, synchronizer tokens for CSRF.

---

## 7. Conclusions and Future Enhancements
The Automated Residential Visitor Gate-Pass System demonstrates a complete, cohesive, and robust implementation of modern software development and DevOps practices. By automating both code integration and infrastructure management, the system guarantees reproducible deployments, rapid fault recovery, and strict quality gates.

**Future Enhancements:**
- Integration of QR code scanning for touchless guard check-in.
- SMS / WhatsApp push notifications via cloud messaging gateways (e.g., Twilio).
- Multi-cluster Kubernetes deployment with Helm chart packaging.
