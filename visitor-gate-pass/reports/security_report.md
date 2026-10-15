# Application Security and Hardening Report

## 1. Executive Summary
Security is a foundational pillar of the **Automated Residential Visitor Gate-Pass System**. Because the application governs physical premises access, visitor record privacy, and resident personal data, security controls must operate effectively across application code, authentication mechanisms, data storage, and containerized runtime infrastructure.

This report documents the security posture, threat model, control implementations, and hardening measures applied to ensure enterprise-grade security.

---

## 2. Threat Modeling (STRIDE Matrix)

| Threat Category | Potential Risk in Gate Pass System | Implemented Countermeasure | Verification Status |
| :--- | :--- | :--- | :---: |
| **Spoofing** | Unauthorized user impersonating a security guard or resident. | Multi-role Spring Security authentication, BCrypt password hashing, session regeneration on login. | Verified |
| **Tampering** | Resident modifying visitor passes belonging to another resident flat. | Server-side role scoping and ownership validation in `WebController`. Residents restricted to `hostResident == principal.name`. | Verified |
| **Repudiation** | Guard denying having checked in a visitor; visitor denying visit. | Immutable timestamp logging (`createdAt`, `checkedInAt`, `checkedOutAt`), unique pass codes (`GP-XXXX`), and audit modal. | Verified |
| **Information Disclosure** | Unauthenticated intruder accessing visitor logs, phone numbers, and resident flats. | Strict authorization filters: `/dashboard`, `/pass/**`, `/resident/**`, and `/api/**` require authentication. | Verified |
| **Denial of Service** | Resource exhaustion via malformed payloads or DB connection starvation. | Database connection pool limits (HikariCP), input length validation, and container resource limits. | Verified |
| **Elevation of Privilege** | Resident executing administrative or guard actions (e.g., checking in visitors). | Endpoint-level role authorization (`@PreAuthorize("hasAnyRole('GUARD', 'ADMIN')")`) preventing horizontal/vertical escalation. | Verified |

---

## 3. Application Security Controls

### 3.1 Authentication & Password Hashing
- **Algorithm:** BCrypt with a default work factor of 10.
- **Salting:** Cryptographically secure random salting generated per-hash automatically by Spring Security's `PasswordEncoder`.
- **Implementation:**
  ```java
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  ```

### 3.2 Role-Based Access Control (RBAC) Matrix
The application enforces strict principle-of-least-privilege RBAC:

| Resource / Endpoint | `ROLE_ADMIN` | `ROLE_GUARD` | `ROLE_RESIDENT` | Anonymous |
| :--- | :---: | :---: | :---: | :---: |
| `/login`, `/css/**`, `/js/**` | Access | Access | Access | Access |
| `/dashboard` | View All Passes | View All Passes | View Own Passes Only | Denied (302) |
| `/pass/create` | Full Create | Full Create | Denied (403) | Denied (302) |
| `/resident/pre-register` | Denied | Denied | Create (Own Flat) | Denied (302) |
| `/pass/approve/{id}` | Approve | Approve | Denied (403) | Denied (302) |
| `/pass/checkin/{id}` | Check-In | Check-In | Denied (403) | Denied (302) |
| `/pass/checkout/{id}` | Check-Out | Check-Out | Denied (403) | Denied (302) |
| `/api/passes/{id}` | View Any | View Any | View Own Flat Only | Denied (401/302) |

### 3.3 Injection & Web Vulnerability Defenses
1. **SQL Injection (SQLi):**
   - All database queries are executed via Spring Data JPA and Hibernate ORM.
   - Dynamic searches utilize strictly parameterized queries with bound variables (`:search`), completely preventing arbitrary SQL string concatenation.
2. **Cross-Site Scripting (XSS):**
   - Thymeleaf templates use standard HTML-escaping attributes (`th:text`) for all dynamic model attributes (e.g., visitor names, purposes, notes).
   - Raw HTML unescaped rendering (`th:utext`) is prohibited across the entire UI codebase.
3. **Cross-Site Request Forgery (CSRF):**
   - Spring Security CSRF protection is enabled.
   - All state-modifying POST requests (`/pass/create`, `/resident/pre-register`, `/pass/approve/**`, etc.) require a valid CSRF synchronizer token rendered automatically by Thymeleaf (`th:action`).

---

## 4. Container & Infrastructure Hardening

### 4.1 Non-Root User Execution
Running containers as the root user introduces severe escalation vulnerabilities if a container breakout occurs. The application `Dockerfile` explicitly provisions and switches to an unprivileged system user:

```dockerfile
# Create unprivileged system group and user
RUN addgroup -S -g 1001 appgroup && adduser -S -u 1001 -G appgroup appuser

# Set working directory ownership
WORKDIR /app
COPY target/*.jar app.jar
RUN chown -R appuser:appgroup /app

# Switch runtime to non-root user
USER appuser
```

### 4.2 Secrets Management & Zero-Trust Hygiene
- No passwords, private keys, or API secrets are committed to version control.
- Configuration is decoupled using environment variable injection (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
- A reference template `.env.example` is maintained, while active `.env` files are ignored via `.gitignore`.
- Database access uses dedicated credentials rather than administrative root permissions where applicable.

---

## 5. Security Audit and Verification Results
- **Authentication Bypass Attempts:** Negative testing in `WebControllerTest` verifies that unauthenticated access redirects to `/login`.
- **Privilege Escalation Attempts:** Resident user attempting to invoke `/pass/checkin/1` receives HTTP 403 Forbidden.
- **Data Scoping:** Resident dashboard query strictly filters `WHERE hostResident = :username`, preventing unauthorized flat viewing.
- **Container Audit:** Verified container running as `appuser` (UID 1001) via Docker inspect.

## 6. Conclusion
The Gate-Pass System complies with modern application security standards, ensuring that data confidentiality, operational integrity, and user authorization are rigorously preserved.
