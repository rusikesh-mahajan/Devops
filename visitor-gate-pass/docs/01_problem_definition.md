# Week 1: Problem Definition and Scope

**Project:** Automated Residential Visitor Gate-Pass System  
**Document Code:** DOC-W01-PDS  
**Version:** 1.0.0  
**Date:** October 14, 2026  

---

## 1. Problem Statement

Modern residential societies, gated communities, and apartment complexes face significant security and operational challenges due to reliance on traditional manual paper logbooks at entry gates. Physical logbooks suffer from several critical shortcomings:
1. **Inefficient Data Collection:** Visitor handwriting is frequently illegible, timestamps are approximated, and contact details cannot be verified in real time.
2. **Security Vulnerabilities:** Paper records provide no digital verification mechanism. Unauthorized visitors can enter false credentials or impersonate residents without immediate detection.
3. **Severe Gate Congestion:** Manual transcription during peak traffic hours (morning deliveries, evening arrivals) generates bottlenecks, increasing wait times at entry points to upwards of 3-5 minutes per vehicle.
4. **Resident Inconvenience:** Guards must phone residents individually using intercoms or cell phones to obtain verbal consent. When residents are unreachable, visitors are either turned away erroneously or granted unverified entry.
5. **Auditing & Traceability Deficits:** In the event of an incident, facility managers require hours or days to physically search through stacks of logbooks, with no capability to filter by visitor name, vehicle registration number, or visit timeframe.
6. **Data Privacy Non-Compliance:** Physical visitor logbooks lie open on guard desks, exposing the names, phone numbers, and destination apartment details of previous visitors and residents to anyone approaching the desk, violating modern data protection standards.

---

## 2. Target Users and Stakeholder Matrix

| Stakeholder Group | Primary Needs & Objectives | Pain Points Addressed | System Interaction |
|:---|:---|:---|:---|
| **Security Guards** | Fast registration interface, immediate resident validation, clear check-in/out timestamps. | Eliminates manual handwriting, reduces gate queues, prevents confrontation over unauthorized entries. | Web tablet / desktop at gate kiosk (`ROLE_GUARD`). |
| **Residents / Hosts** | Pre-approve expected guests/deliveries, approve or reject gate requests instantly, track guest history. | Eliminates disruptive intercom calls during work/sleep; provides transparency over who enters for their unit. | Mobile / resident portal dashboard (`ROLE_RESIDENT`). |
| **System Administrators** | Society-wide oversight, audit logging, role management, daily summary metrics. | Eliminates lost paperwork, enables instant incident investigation, provides operational analytics. | Administrative control portal (`ROLE_ADMIN`). |
| **Visitors / Delivery Personnel** | Seamless, swift entry without repetitive interrogation. | Decreases waiting time at gate from >3 minutes to under 30 seconds. | Receives unique pass identifier (e.g. `GP-1001`). |
| **Law Enforcement / Auditors** | Reliable, immutable digital logs for security investigations. | High data integrity, exact check-in and check-out timestamps, tamper-resistant digital records. | Exportable incident reports and audit queries. |

---

## 3. Project Objectives and Measurable Success Criteria

### Primary Objectives
- Automate the end-to-end visitor lifecycle from registration to checkout.
- Enforce role-based access control with discrete privileges for guards, residents, and admins.
- Establish an automated DevOps pipeline supporting continuous testing, containerized packaging, automated deployment, and configuration management.

### Measurable Key Performance Indicators (KPIs)
- **Check-In Velocity:** Reduce average visitor processing time from 180 seconds (manual) to **under 45 seconds** (digital).
- **Adoption Rate:** Achieve **100% digital transition** of gate visitor tracking within 14 days of operational rollout.
- **Service Availability:** Maintain **99.9% uptime** across continuous operational shifts supported by container health recovery.
- **Workflow Approval Latency:** Enable resident approval/rejection decisions within **30 seconds** of gate pass creation.
- **Search Retrieval Time:** Retrieve any historical record by pass code, visitor name, vehicle number, or date in **< 1.0 second**.

---

## 4. Constraints, Risks, and Assumptions

### Constraints
- **Technical Constraints:** Front gate kiosks often operate on lower-specification hardware (tablets/laptops). The frontend must be lightweight, responsive, and cross-browser compatible.
- **Network Constraints:** Gatehouses may encounter transient network fluctuations; local database caching and container restart resilience are required.
- **Budget Constraints:** Zero commercial software licensing; built entirely using proven open-source technologies (Java LTS, Spring Boot, MySQL, Docker, Jenkins, Ansible).
- **Time Constraints:** 15-week academic and operational delivery timeline.

### Risk Mitigation Matrix
| Identified Risk | Severity | Probability | Mitigation Strategy |
|:---|:---|:---|:---|
| Network timeout between application and database | High | Medium | Implemented container health checks and connection retry pools in Spring Boot / HikariCP. |
| Unauthorized status manipulation | Critical | Low | Enforced server-side state machine validation; resident can only approve their own apartment requests. |
| Test failures in automated deployment | High | Medium | Integrated automated Selenium UI tests and Maven unit tests as strict Jenkins pipeline gates. |
| Data loss across container recreation | High | Low | Configured persistent named Docker volumes (`db-data:/var/lib/mysql`) for database durability. |

---

## 5. Approved 15-Week MVP Scope

The Minimum Viable Product (MVP) implements the following end-to-end capabilities:
1. **Authentication & RBAC:** Secure session authentication with BCrypt hashing supporting Admin, Guard, and Resident roles.
2. **Visitor Registration:** Gate entry registration by guards and pre-registration by residents with pass codes (`GP-XXXX`).
3. **Role-Based State Machine:** Enforced state progression: `PENDING → APPROVED / REJECTED → CHECKED_IN → CHECKED_OUT`.
4. **Summary Dashboard & Search:** Real-time metrics (Total, Pending, Approved, Inside, Checked Out, Rejected) and multi-field search.
5. **Continuous Integration & Delivery:** Automated Jenkins pipeline executing unit tests, Selenium UI journeys, Docker packaging, and deployment.
