# Comprehensive Viva Voce Questions & Answers
## Automated Residential Visitor Gate-Pass System (15-Week DevOps Capstone)

This document contains a structured compilation of technical viva voce questions and in-depth answers across all 15 weeks of the DevOps curriculum. It serves as a thorough preparation guide for college evaluations, defense panels, and technical interviews.

---

## Category 1: Application Architecture & Spring Boot 3

### Q1. Why did you choose Spring Boot 3.2.4 and Java 21 LTS for this application?
**Answer:**
Java 21 is a Long-Term Support (LTS) release providing modern JVM features such as virtual threads (Project Loom), enhanced pattern matching, record classes, and improved memory management. Spring Boot 3.2.4 provides native support for Java 21, built-in production readiness via Spring Boot Actuator, out-of-the-box auto-configuration for Spring Data JPA and Spring Security 6, and streamlined containerization through efficient JRE layering.

### Q2. How is the application layered, and why is this separation important?
**Answer:**
The application utilizes a classic enterprise layered architecture:
1. **Entity Layer (`com.gatepass.entity`):** Defines domain models (`VisitorPass`, `User`, `Role`) mapped to relational database tables using JPA annotations.
2. **Repository Layer (`com.gatepass.repository`):** Extends `JpaRepository` to provide database abstraction, derived queries, and optimized JPQL searches.
3. **Controller / Web Layer (`com.gatepass.controller`):** Handles incoming HTTP requests, enforces role-based access control, coordinates business workflows, and binds data to Thymeleaf view templates.
4. **Configuration Layer (`com.gatepass.config`):** Encapsulates security filters, password encoding beans, and data initialization (`DataInitializer`).

This separation of concerns ensures testability (e.g., mocking controller dependencies without spinning up a real database), maintainability, and clean decoupling of business logic from infrastructure.

### Q3. How does the Visitor Pass state machine work, and how do you prevent invalid transitions?
**Answer:**
The pass workflow consists of four discrete states:
- `PENDING`: Initial state for walk-in visitors registered by guards awaiting verification.
- `APPROVED`: Pre-registered passes by residents or approved by guards.
- `REJECTED`: Passes denied entry by residents or guards.
- `CHECKED_IN`: Visitor has entered the physical premises.
- `CHECKED_OUT`: Visitor has departed.

Invalid transitions are prevented through two defensive layers:
1. **Entity-Level Predicates:** `VisitorPass` defines helper methods (`canApprove()`, `canReject()`, `canCheckIn()`, `canCheckOut()`, `canEdit()`) that return booleans based on current state.
2. **Controller-Level Enforcement:** In `WebController`, before any state modification, the controller checks the respective predicate. If invalid (e.g., attempting to check in a pass that is still `PENDING` or already `CHECKED_OUT`), the request is rejected with a descriptive error message and redirected or returned as HTTP 400 Bad Request.

---

## Category 2: Security & Authentication (Spring Security)

### Q4. How is Role-Based Access Control (RBAC) implemented in the system?
**Answer:**
We use Spring Security 6.2 with in-memory or database-backed authentication. Three user roles are defined:
- `ROLE_ADMIN`: Full administrative control over all passes, users, and configuration.
- `ROLE_GUARD`: Gate-level operations including walk-in pass creation, approval, check-in, check-out, and global search.
- `ROLE_RESIDENT`: Restricted access scoped exclusively to passes where `hostResident` matches the authenticated username. Residents can pre-register visitors but cannot approve walk-ins or perform check-in/out.

Authorization rules are declared via `SecurityFilterChain` bean:
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/css/**", "/js/**", "/login").permitAll()
    .requestMatchers("/pass/checkin/**", "/pass/checkout/**").hasAnyRole("GUARD", "ADMIN")
    .requestMatchers("/resident/**").hasRole("RESIDENT")
    .requestMatchers("/dashboard").authenticated()
    .anyRequest().authenticated()
);
```

### Q5. How do you prevent horizontal privilege escalation between residents?
**Answer:**
Horizontal privilege escalation occurs when a resident attempts to view or modify passes hosted by a different flat resident. We mitigate this in `WebController` by retrieving the authenticated `Principal`:
```java
if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESIDENT"))) {
    passes = passRepository.findByHostResidentOrderByCreatedAtDesc(auth.getName());
}
```
Furthermore, the details API (`/api/passes/{id}`) checks if the requester is a resident and enforces `if (!pass.getHostResident().equals(auth.getName())) throw new AccessDeniedException();`.

### Q6. How are passwords secured and how is CSRF mitigated?
**Answer:**
- **Passwords:** Passwords are hashed using BCrypt (`BCryptPasswordEncoder`) with a default cost factor of 10. Passwords are never stored or logged in plain text.
- **CSRF:** Cross-Site Request Forgery is protected using Spring Security's synchronizer token pattern. All HTML forms rendered via Thymeleaf automatically embed a hidden `_csrf` input token. If a state-modifying POST request lacks a matching token, it is rejected with HTTP 403 Forbidden.

---

## Category 3: Continuous Integration & Jenkins CI/CD

### Q7. Explain the structure of your Jenkinsfile and what each stage accomplishes.
**Answer:**
Our `Jenkinsfile` utilizes declarative pipeline syntax structured into 9 sequential stages:
1. **Initialize & Check Environment:** Validates Docker, Java 21, and Maven tooling availability.
2. **Compile & Unit Tests:** Runs `mvn clean test` to validate repository and controller logic before packaging.
3. **Package & Quality Gate:** Builds the executable Spring Boot JAR file (`mvn package -DskipTests`).
4. **Build Docker Image:** Builds the hardened Alpine-based Docker container tagged with build number and `latest`.
5. **Deploy Multi-Container Stack:** Executes `docker compose up -d` with project isolation (`-p visitor-gate-pass`).
6. **Health Check & Verification:** Polls `http://localhost:8080/actuator/health` up to 12 times (60s) to guarantee application startup before declaring the stage successful.
7. **Selenium E2E UI Tests:** (Optional/Parameterized) Executes headless Chrome browser journeys against the deployed instance.
8. **Archive Artifacts:** Archives test reports and UI screenshots for audit trails.
9. **Teardown & Notifications:** Cleans up temporary resources and logs pipeline execution status.

### Q8. What happens if a unit test fails during the Jenkins pipeline run?
**Answer:**
Because of strict pipeline test gating, if any test in Stage 2 (`Compile & Unit Tests`) fails, Maven exits with a non-zero exit code. Jenkins immediately aborts the pipeline, marks the build status as `FAILURE` (red), logs the stack trace in the console output, and suppresses subsequent stages (Docker image creation and deployment are never triggered). This prevents broken code from ever being packaged or deployed.

---

## Category 4: Automated Testing & Selenium

### Q9. What is the difference between your MockMvc tests and your Selenium tests?
**Answer:**
- **MockMvc (`WebControllerTest`):** Runs inside the Spring test context without starting an actual HTTP server or browser engine. It mocks the servlet container, testing controller routing, request mapping, Spring Security filters, and model attributes with high speed (~10-50 milliseconds per test).
- **Selenium WebDriver (`GatePassUITest`):** Starts a real embedded web server on a random port and launches a real headless Google Chrome browser. It tests the end-to-end user experience, including JavaScript execution, CSS rendering, DOM event handling, form submission, and modal animations.

### Q10. How did you run Selenium tests in a headless automated environment?
**Answer:**
In `GatePassUITest.java`, Chrome options are configured for headless execution:
```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");
options.addArguments("--disable-dev-shm-usage");
options.addArguments("--window-size=1920,1080");
WebDriver driver = new ChromeDriver(options);
```
Using `--headless=new` allows the browser to execute on CI servers or developer machines without requiring a physical monitor or graphical desktop session, while capturing full-resolution screenshots (`TakesScreenshot`).

---

## Category 5: Containerization & Docker Orchestration

### Q11. How did you optimize and secure your Dockerfile?
**Answer:**
1. **Base Image Selection:** Used `eclipse-temurin:21-jre-alpine` which has a minimal attack surface and small footprint (~180MB) compared to full JDK or Debian-based images (~500MB+).
2. **Non-Root Execution:** Provisioned an unprivileged group and user (`appuser` with UID 1001) and executed the container via `USER appuser`.
3. **Built-In Healthcheck:** Configured `HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1`.

### Q12. How did you solve the database connection race condition in Docker Compose?
**Answer:**
By default, Docker Compose's `depends_on: [db]` only waits for the database container to start running, not for MySQL to finish initializing tables and accepting connections. This caused Spring Boot to crash with `Communications link failure`.

We solved this by:
1. Adding a health check on the `db` service:
   ```yaml
   healthcheck:
     test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
     interval: 5s
     timeout: 5s
     retries: 10
   ```
2. Specifying a conditioned dependency on the `app` service:
   ```yaml
   depends_on:
     db:
       condition: service_healthy
   ```
This ensures Spring Boot is only launched after MySQL is fully initialized and responding to ping requests.

---

## Category 6: Configuration Management & Ansible

### Q13. What is idempotency in Ansible and why is it critical?
**Answer:**
Idempotency is the property of an operation whereby executing it multiple times produces the exact same system state as executing it once, without causing unintended side effects. In our `ansible/playbook.yml`, if Docker containers are already running and configurations match target state, Ansible reports `ok` and `changed=0`. This ensures scheduled runs or re-deployments do not restart running containers unnecessarily or cause service disruption.

### Q14. How does your rollback playbook work?
**Answer:**
Our rollback mechanism (`ansible/rollback.yml`):
1. Detects a deployment degradation (e.g., container crash or failing `/actuator/health` probe).
2. Stops the degraded application container.
3. Re-tags or pulls the known stable release image (e.g., `visitor-gate-pass:previous` or `v1.0.0`).
4. Re-launches the container stack and polls the Actuator health endpoint until operational status `UP` is verified, restoring service within under 30 seconds.

---

## Category 7: Git & DevOps Methodology

### Q15. Explain your Gitflow branching and release tagging strategy.
**Answer:**
We adopted standard Gitflow:
- `main`: Stores official production releases. Only tagged release commits (`v1.0.0`) are merged here.
- `develop`: Integration branch where features are aggregated and validated before release.
- `feature/*`: Short-lived branches where individual user stories or bug fixes are developed.
- **Semantic Versioning (SemVer):** Releases follow `MAJOR.MINOR.PATCH` (e.g., `v1.0.0`). Git tags are signed and annotated with release changelogs.
