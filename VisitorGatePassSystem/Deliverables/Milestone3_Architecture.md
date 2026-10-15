# Milestone 3: Requirements, Architecture and Technology Setup

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Software Requirements Specification (SRS) Summary

#### 1.1 Functional Requirements

| ID | Requirement | Module |
|----|------------|--------|
| FR-01 | The system shall allow Guards to create visitor passes with name, phone, purpose, vehicle number, and host resident. | Visitor Management |
| FR-02 | The system shall set initial pass status to "PENDING" upon creation. | Workflow Engine |
| FR-03 | The system shall allow Residents to approve or reject pending visitor passes assigned to their apartment. | Approval Workflow |
| FR-04 | The system shall allow Guards to check-in approved visitors (records timestamp). | Check-In/Out |
| FR-05 | The system shall allow Guards to check-out checked-in visitors (records timestamp). | Check-In/Out |
| FR-06 | The system shall display a dashboard with Total Passes, Pending, and Currently Checked-In counts for Admin users. | Dashboard |
| FR-07 | The system shall provide search functionality by visitor name, status, or date for Admin users. | Search |
| FR-08 | The system shall enforce role-based access control (ADMIN, GUARD, RESIDENT). | Authentication |
| FR-09 | The system shall provide secure login/logout functionality using Spring Security. | Authentication |
| FR-10 | The system shall persist all data in a MySQL relational database. | Data Persistence |

#### 1.2 Non-Functional Requirements

| ID | Requirement | Category |
|----|------------|----------|
| NFR-01 | The system shall respond to any user action within 2 seconds. | Performance |
| NFR-02 | The system shall be accessible via any modern web browser (Chrome, Firefox, Edge). | Compatibility |
| NFR-03 | The system shall run on low-end devices (tablets/smartphones) used by guards. | Accessibility |
| NFR-04 | The system shall maintain 99.9% uptime during operational hours. | Reliability |
| NFR-05 | Passwords shall be encrypted using BCrypt hashing. | Security |
| NFR-06 | The system shall be deployable via Docker containers. | Deployability |

---

### 2. Use-Case Diagram

```
                    ┌─────────────────────────────────────┐
                    │   Visitor Gate-Pass System           │
                    │                                     │
  ┌───────┐         │  ┌─────────────────────────┐       │
  │       │         │  │     Login / Logout       │       │
  │ Guard │────────▶│  └─────────────────────────┘       │
  │       │────┐    │  ┌─────────────────────────┐       │
  └───────┘    │    │  │  Create Visitor Pass     │       │
               ├───▶│  └─────────────────────────┘       │
               │    │  ┌─────────────────────────┐       │
               ├───▶│  │  Check-In Visitor        │       │
               │    │  └─────────────────────────┘       │
               ├───▶│  │  Check-Out Visitor       │       │
               │    │  └─────────────────────────┘       │
               │    │  ┌─────────────────────────┐       │
               │    │  │  View Visitor Records    │       │
               └───▶│  └─────────────────────────┘       │
                    │                                     │
  ┌──────────┐      │  ┌─────────────────────────┐       │
  │          │─────▶│  │  View Pending Requests   │       │
  │ Resident │      │  └─────────────────────────┘       │
  │          │─────▶│  ┌─────────────────────────┐       │
  └──────────┘      │  │  Approve / Reject Pass   │       │
                    │  └─────────────────────────┘       │
                    │                                     │
  ┌───────┐         │  ┌─────────────────────────┐       │
  │       │────────▶│  │  View Dashboard Stats    │       │
  │ Admin │         │  └─────────────────────────┘       │
  │       │────────▶│  ┌─────────────────────────┐       │
  └───────┘         │  │  Search Visitor Records  │       │
                    │  └─────────────────────────┘       │
                    └─────────────────────────────────────┘
```

---

### 3. Application Architecture Diagram (3-Tier)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION TIER                          │
│                                                                     │
│   Browser (Chrome/Firefox/Edge)                                    │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │  Thymeleaf Templates + Bootstrap 5 + Custom CSS             │  │
│   │  • login.html    → Login form                               │  │
│   │  • dashboard.html → Role-based dashboard with data tables   │  │
│   └─────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP (Port 8080)
┌──────────────────────────────▼──────────────────────────────────────┐
│                        APPLICATION TIER                             │
│                                                                     │
│   Spring Boot 3.2.4  (Embedded Tomcat)                             │
│   ┌──────────────────┐  ┌──────────────────┐  ┌────────────────┐  │
│   │  WebController   │  │  SecurityConfig  │  │ DataInitializer│  │
│   │  (MVC Routes)    │  │  (Auth + RBAC)   │  │ (Seed Data)    │  │
│   └────────┬─────────┘  └──────────────────┘  └────────────────┘  │
│            │                                                        │
│   ┌────────▼─────────┐  ┌──────────────────────────────────────┐  │
│   │  JPA Repositories│  │  Spring Security UserDetailsService  │  │
│   │  • UserRepo      │  │  (BCrypt password encoding)          │  │
│   │  • VisitorPassRepo│ └──────────────────────────────────────┘  │
│   └────────┬─────────┘                                             │
│            │ JPA/Hibernate                                         │
└────────────┼───────────────────────────────────────────────────────┘
             │ JDBC (Port 3306)
┌────────────▼───────────────────────────────────────────────────────┐
│                          DATA TIER                                 │
│                                                                     │
│   MySQL 8.0 (Docker Container)                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │  Database: gatepass_db                                      │  │
│   │  ┌────────────────┐     ┌──────────────────────────────┐   │  │
│   │  │  users          │     │  gate_passes                 │   │  │
│   │  │  • id (PK)      │◀────│  • host_id (FK → users.id)  │   │  │
│   │  │  • username     │     │  • id (PK)                   │   │  │
│   │  │  • password     │     │  • visitor_name              │   │  │
│   │  │  • role         │     │  • phone, purpose            │   │  │
│   │  │  • name         │     │  • vehicle_no, status        │   │  │
│   │  └────────────────┘     │  • created_at                 │   │  │
│   │                          │  • check_in_time              │   │  │
│   │                          │  • check_out_time             │   │  │
│   │                          └──────────────────────────────┘   │  │
│   └─────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

---

### 4. Data Model

#### Table: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(255) | UNIQUE, NOT NULL | Login username |
| password | VARCHAR(255) | NOT NULL | BCrypt-hashed password |
| role | VARCHAR(255) | NOT NULL | ROLE_ADMIN, ROLE_GUARD, ROLE_RESIDENT |
| name | VARCHAR(255) | NOT NULL | Display name |

#### Table: `gate_passes`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique pass identifier |
| visitor_name | VARCHAR(255) | NOT NULL | Visitor's full name |
| phone | VARCHAR(255) | NOT NULL | Visitor's phone number |
| purpose | VARCHAR(255) | | Purpose of visit |
| vehicle_no | VARCHAR(255) | | Vehicle registration number |
| status | VARCHAR(255) | NOT NULL | PENDING / APPROVED / REJECTED / CHECKED_IN / CHECKED_OUT |
| host_id | BIGINT | FK → users.id, NOT NULL | Resident who is being visited |
| created_at | DATETIME | | Auto-set on creation |
| check_in_time | DATETIME | | Set when guard checks-in visitor |
| check_out_time | DATETIME | | Set when guard checks-out visitor |

#### Entity Relationship: `users (1) ←──── (N) gate_passes`

---

### 5. API / Route List

| Method | Route | Role | Description |
|--------|-------|------|-------------|
| GET | `/login` | Public | Show login page |
| POST | `/login` | Public | Spring Security form login |
| POST | `/logout` | Authenticated | End session, redirect to login |
| GET | `/dashboard` or `/` | Authenticated | Role-based dashboard |
| POST | `/guard/create-pass` | GUARD | Create new visitor pass |
| POST | `/guard/update-status/{id}` | GUARD | Check-in or Check-out visitor |
| POST | `/resident/update-status/{id}` | RESIDENT | Approve or Reject visitor pass |
| GET | `/admin/search?query=` | ADMIN | Search visitor records |
| GET | `/actuator/health` | Public | Health check endpoint |

---

### 6. Technology Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 (OpenJDK) |
| Framework | Spring Boot | 3.2.4 |
| Web Server | Embedded Tomcat | (via Spring Boot) |
| Templating | Thymeleaf | (via Spring Boot Starter) |
| Security | Spring Security | 6.x |
| ORM | Hibernate / JPA | 6.4.4 |
| Database | MySQL | 8.0 |
| Test DB | H2 (in-memory) | 2.2.x |
| CSS Framework | Bootstrap | 5.3.0 |
| Build Tool | Apache Maven | 3.x |
| Testing | JUnit 5 + Selenium WebDriver | 4.19.1 |
| CI/CD | Jenkins | 2.x |
| Containerization | Docker + Docker Compose | Latest |
| Config Mgmt | Ansible | 2.x |
| Version Control | Git + GitHub | Latest |
| Monitoring | Spring Boot Actuator | (via Spring Boot Starter) |

---

### 7. Local Development Setup

```bash
# Prerequisites: Java 17, Maven, MySQL 8.0 (or Docker)

# Option A: Use Docker for MySQL
docker-compose up -d db

# Option B: Local MySQL — create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gatepass_db;"

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Access at http://localhost:8080/login
# Demo credentials:
#   Admin:    admin / admin123
#   Guard:    guard1 / guard123
#   Resident: resident1 / res123
```
