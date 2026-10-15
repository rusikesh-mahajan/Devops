# Milestone 6: MVP Completion — Full CRUD, Dashboard & Search

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Complete all remaining MVP user stories to deliver a fully functional Visitor Gate-Pass application. Implement resident approval/rejection workflow, guard check-in/check-out, admin dashboard with statistics, and search functionality. Demonstrate a **merge conflict resolution** and create a **version tag**.

---

### 2. User Stories Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-02 | As a **Resident**, I want to view pending visitor requests for my apartment so that I can approve or reject them. | HIGH | ✅ DONE |
| US-03 | As a **Guard**, I want to check-in an approved visitor so that I can record their actual entry time. | HIGH | ✅ DONE |
| US-04 | As a **Guard**, I want to check-out a visitor so that I can record their departure time. | HIGH | ✅ DONE |
| US-05 | As an **Admin**, I want to see a summary dashboard with total, pending, and active visitor counts. | HIGH | ✅ DONE |
| US-06 | As an **Admin**, I want to search visitor records by name, status, or date so that I can retrieve historical data. | HIGH | ✅ DONE |
| US-08 | As a **User**, I want to log out securely so that my session is terminated. | MEDIUM | ✅ DONE |
| US-09 | As a **Guard**, I want to see all visitor records in a table so that I can track current visitors. | MEDIUM | ✅ DONE |

---

### 3. Implementation Details

#### 3.1 US-02 — Resident Approval/Rejection

**Endpoint:** `POST /resident/update-status/{id}`

**Flow:**
```
Resident logs in → Dashboard shows passes assigned to their apartment
    → Sees PENDING passes with "Approve" and "Reject" buttons
    → Clicks "Approve" → Status changes to APPROVED
    → Clicks "Reject"  → Status changes to REJECTED
    → Page refreshes → Buttons disappear for processed passes
```

**Key Implementation (WebController.java):**
```java
@PostMapping("/resident/update-status/{id}")
public String updateStatusResident(@PathVariable Long id, @RequestParam String status) {
    VisitorPass pass = passRepo.findById(id).orElseThrow();
    if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
        pass.setStatus(status);
        passRepo.save(pass);
    }
    return "redirect:/dashboard";
}
```

**Acceptance Criteria Verification:**
- ✅ Resident sees only passes assigned to them (`findByHostId()`).
- ✅ "Approve" and "Reject" buttons appear only for PENDING passes.
- ✅ Status updates immediately upon clicking.

#### 3.2 US-03 & US-04 — Guard Check-In / Check-Out

**Endpoint:** `POST /guard/update-status/{id}`

**Flow:**
```
Guard logs in → Sees visitor records table
    → APPROVED pass shows "Check In" button
        → Click → Status = CHECKED_IN, checkInTime = now()
    → CHECKED_IN pass shows "Check Out" button
        → Click → Status = CHECKED_OUT, checkOutTime = now()
```

**Acceptance Criteria Verification:**
- ✅ "Check-In" button visible only for APPROVED passes.
- ✅ Clicking records `checkInTime` timestamp.
- ✅ "Check-Out" button visible only for CHECKED_IN passes.
- ✅ Clicking records `checkOutTime` timestamp.

#### 3.3 US-05 — Admin Dashboard Statistics

**Three Stat Cards:**

| Card | Data Source | Style |
|------|-----------|-------|
| Total Passes | `passRepo.count()` | Blue accent (stat-primary) |
| Pending Approval | `passRepo.findByStatus("PENDING").size()` | Amber accent (stat-warning) |
| Currently Inside | `passRepo.findByStatus("CHECKED_IN").size()` | Green accent (stat-success) |

**Acceptance Criteria Verification:**
- ✅ Dashboard shows three stat cards with real-time counts.
- ✅ Cards use hover animations and gradient backgrounds.

#### 3.4 US-06 — Admin Search

**Endpoint:** `GET /admin/search?query=`

**Implementation:** Filters all passes by visitor name, status, or date (case-insensitive string matching using Java streams).

```java
List<VisitorPass> results = passRepo.findAll().stream()
    .filter(p -> p.getVisitorName().toLowerCase().contains(lowerQuery)
            || p.getStatus().toLowerCase().contains(lowerQuery)
            || (p.getCreatedAt() != null && p.getCreatedAt().toString().contains(lowerQuery)))
    .toList();
```

**Acceptance Criteria Verification:**
- ✅ Search box filters records by name, status, or date.
- ✅ Results update in the table below.
- ✅ "Clear" button resets the search and shows all records.

#### 3.5 US-08 — Secure Logout

**Configuration (SecurityConfig.java):**
```java
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout")
    .permitAll()
)
```

**Acceptance Criteria Verification:**
- ✅ Logout button visible in navbar for all authenticated users.
- ✅ Session is terminated on logout.
- ✅ Redirects to login page with "You have been logged out" message.

#### 3.6 US-09 — Guard Visitor Records Table

**Table Columns:**

| Column | Data | Notes |
|--------|------|-------|
| ID | `#` + pass.id | Muted text |
| Visitor | Name + Purpose | Two-line cell |
| Contact | Phone number | — |
| Host | Resident name | From `pass.host.name` |
| Status | Badge | Color-coded by status |
| Action | Buttons | Role-dependent actions |

**Status Badge Colors:**
| Status | Badge Class | Color |
|--------|------------|-------|
| PENDING | `bg-warning` | Amber |
| APPROVED | `bg-info` | Blue |
| CHECKED_IN | `bg-success` | Green |
| REJECTED | `bg-danger` | Red |
| CHECKED_OUT | `bg-secondary` | Gray |

---

### 4. Merge Conflict Resolution Demo

A merge conflict was simulated to demonstrate the team's conflict resolution workflow.

**Scenario:** Two developers modified the same section of `dashboard.html` — one changed the navbar title and another changed the stat card labels.

**Resolution Steps:**
```bash
# 1. Attempt merge
git checkout develop
git merge feature/visitor-pass-crud

# 2. Conflict detected in dashboard.html
# Auto-merging src/main/resources/templates/dashboard.html
# CONFLICT (content): Merge conflict in dashboard.html

# 3. Open file and resolve manually
# Kept both changes, integrated navbar title update with stat card improvements

# 4. Mark as resolved and commit
git add src/main/resources/templates/dashboard.html
git commit -m "fix(merge): resolve conflict in dashboard.html navbar and stat cards"
```

---

### 5. Version Tag

After merging all MVP features into `develop` and then into `main`:

```bash
# Tag the MVP release
git checkout main
git merge develop
git tag -a v1.0.0 -m "Release v1.0.0: MVP with full visitor gate-pass workflow"
git push origin v1.0.0
```

| Property | Value |
|----------|-------|
| Tag Name | `v1.0.0` |
| Description | MVP with full visitor gate-pass workflow |
| Includes | US-01 through US-10, login, CRUD, dashboard, search |

---

### 6. Complete Visitor Workflow (End-to-End)

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Guard   │───▶│ Create Pass  │───▶│   Resident   │───▶│    Guard     │───▶│    Guard     │
│  Login   │    │ (PENDING)    │    │  Approves    │    │  Check-In   │    │  Check-Out  │
│          │    │              │    │ (APPROVED)   │    │ (CHECKED_IN)│    │(CHECKED_OUT)│
└──────────┘    └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                                          │
                                          ├──▶ OR Rejects (REJECTED) → Workflow ends
                                          │
                                    ┌─────▼─────┐
                                    │   Admin    │
                                    │  Views     │
                                    │ Dashboard  │
                                    │ & Searches │
                                    └───────────┘
```

---

### 7. Definition of Done — Checklist

- [x] **Code Complete** — All 7 user stories implemented.
- [x] **Tests Pass** — Application compiles and starts; Selenium tests pass.
- [x] **Code Reviewed** — Changes merged via PR.
- [x] **Committed** — Meaningful commit messages following convention.
- [x] **Documentation Updated** — README reflects full feature set.
- [x] **Pipeline Green** — Jenkinsfile configured with all stages.
- [x] **Deployed** — Full MVP deployable via `mvn spring-boot:run` and `docker-compose up`.
- [x] **Acceptance Criteria Met** — All criteria for US-02 through US-09 verified.
- [x] **Merge Conflict** — Demonstrated and resolved.
- [x] **Version Tag** — `v1.0.0` created on `main`.
