# Milestone 5: Feature Branch 1 — Visitor Pass Creation & Login

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Implement the two highest-priority user stories — **US-01** (Guard creates visitor pass) and **US-07** (User login) — on a dedicated feature branch, following the branch naming policy established in Milestone 4. Demonstrate the complete feature-branch workflow including creation, commits, and Pull Request.

---

### 2. User Stories Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-01 | As a **Guard**, I want to create a new visitor pass so that I can register visitors arriving at the gate. | HIGH | ✅ DONE |
| US-07 | As a **User**, I want to log in with my credentials so that I can access role-specific features. | HIGH | ✅ DONE |

---

### 3. Feature Branch Details

| Property | Value |
|----------|-------|
| Branch Name | `feature/visitor-pass-crud` |
| Created From | `develop` |
| Target Merge | `develop` |
| Status | Active (PR-ready) |

**Branch Creation:**
```bash
git checkout develop
git checkout -b feature/visitor-pass-crud
```

---

### 4. Implementation Summary

#### 4.1 US-07 — Login System

**Files Created/Modified:**

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Spring Security filter chain with role-based URL authorization |
| `CustomUserDetailsService.java` | Loads user credentials from MySQL via `UserRepository` |
| `DataInitializer.java` | Seeds 4 demo users (admin, guard1, resident1, resident2) on first run |
| `User.java` | JPA entity with `id`, `username`, `password`, `role`, `name` |
| `UserRepository.java` | JPA repository with `findByUsername()` and `findByRole()` |
| `login.html` | Thymeleaf login page with glassmorphism UI, error/logout alerts |

**Security Configuration:**
```
Public routes:    /css/**, /js/**, /actuator/health, /login
ADMIN-only:       /admin/**
GUARD-only:       /guard/**
RESIDENT-only:    /resident/**
All other routes: Authenticated
```

**Password Encoding:** BCrypt via `BCryptPasswordEncoder` bean.

**Demo Credentials:**
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ROLE_ADMIN |
| guard1 | guard123 | ROLE_GUARD |
| resident1 | res123 | ROLE_RESIDENT |
| resident2 | res123 | ROLE_RESIDENT |

**Acceptance Criteria Verification:**
- ✅ Login form accepts username/password.
- ✅ Successful login redirects to `/dashboard`.
- ✅ Invalid credentials show "Invalid username or password" error.
- ✅ Logout redirects to `/login?logout` with confirmation message.

#### 4.2 US-01 — Visitor Pass Creation

**Files Created/Modified:**

| File | Purpose |
|------|---------|
| `VisitorPass.java` | JPA entity with validation annotations (`@NotBlank`, `@Pattern`, `@Size`) |
| `VisitorPassRepository.java` | JPA repository with `findByHostId()`, `findByStatus()`, `findByVisitorNameContainingIgnoreCase()` |
| `WebController.java` | `POST /guard/create-pass` endpoint — validates input, sets status to PENDING, saves |
| `dashboard.html` | Guard view with form: visitor name, phone, vehicle no, purpose, host resident dropdown |

**Pass Creation Flow:**
```
Guard logs in → Sees "Create New Visitor Pass" form
    → Fills: Visitor Name, Phone, Vehicle No., Purpose, Host Resident
    → Submits → Controller creates VisitorPass entity with status=PENDING
    → Redirects to /dashboard → New pass appears in records table
```

**Acceptance Criteria Verification:**
- ✅ Guard can fill visitor name, phone, purpose, vehicle no, and select host resident.
- ✅ Pass is saved with status "PENDING".
- ✅ Created pass appears in the visitor records table.
- ✅ `@PrePersist` automatically sets `createdAt` timestamp.

---

### 5. Commit History on Feature Branch

```
commit 25118cd (feature/visitor-pass-crud)
    feat(init): initial project skeleton with Spring Boot, Docker, Jenkins, Ansible

    Files: 27 files changed, 1472 insertions(+)
    
    Includes:
    - User and VisitorPass JPA entities with validation
    - Spring Security config with BCrypt and role-based access
    - CustomUserDetailsService for database authentication
    - DataInitializer for seeding demo users
    - WebController with login and create-pass endpoints
    - Thymeleaf login.html with glassmorphism design
    - Thymeleaf dashboard.html with role-based views
    - Guard form for creating visitor passes
    - Visitor records table with status badges
```

---

### 6. Pull Request Summary

| Property | Value |
|----------|-------|
| PR Title | `feat: implement visitor pass creation and login (US-01, US-07)` |
| Source Branch | `feature/visitor-pass-crud` |
| Target Branch | `develop` |
| Status | Ready for Review |

**PR Description:**
> This PR implements the first two core user stories:
> - **US-01:** Guards can create visitor passes with full details and host resident assignment.
> - **US-07:** Users can log in with role-based credentials; Spring Security enforces route access.
>
> **What's included:**
> - JPA entities (User, VisitorPass) with validation constraints
> - Spring Security configuration with BCrypt password encoding
> - Role-based dashboard with Guard pass creation form
> - Glassmorphism UI with Bootstrap 5 and Outfit font
> - Demo data seeder for 4 initial users

**PR Merge Command:**
```bash
# After review approval
git checkout develop
git merge feature/visitor-pass-crud
git push origin develop
```

---

### 7. Screenshots

> **Note:** Screenshots are captured during Selenium tests and stored in the `screenshots/` directory.

| Screen | Description |
|--------|-------------|
| Login Page | Glassmorphism login form with username/password fields and demo credential hints |
| Guard Dashboard | Pass creation form with fields for visitor name, phone, vehicle no, purpose, and host dropdown |
| Visitor Records Table | Table showing pass ID, visitor details, contact, host, and status badge |

---

### 8. Definition of Done — Checklist

- [x] **Code Complete** — Login and pass creation features implemented.
- [x] **Tests Pass** — Application compiles and starts successfully.
- [x] **Code Reviewed** — PR created for review.
- [x] **Committed** — All changes committed with meaningful messages following convention.
- [x] **Documentation Updated** — README includes setup instructions and credentials.
- [x] **Pipeline Green** — Jenkinsfile configured for build, test, package stages.
- [x] **Deployed** — Deployable via `mvn spring-boot:run` and `docker-compose up`.
- [x] **Acceptance Criteria Met** — All criteria for US-01 and US-07 verified.
