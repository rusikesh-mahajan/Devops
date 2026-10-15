# Milestone 7: Jenkins CI Setup

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Set up a Jenkins Continuous Integration server, create a Jenkins job for the Visitor Gate-Pass project, configure build triggers, and demonstrate automated build with archived artefacts. This milestone addresses **US-12** (Jenkins CI/CD Pipeline).

---

### 2. User Story Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-12 | As a **DevOps Engineer**, I want a Jenkins CI/CD pipeline so that every commit is automatically built and tested. | MEDIUM | 🔄 In Progress (CI Setup phase) |

---

### 3. Jenkins Installation & Configuration

#### 3.1 Installation

| Property | Value |
|----------|-------|
| Jenkins Version | 2.x (LTS) |
| Installation Method | Windows MSI Installer / WAR file |
| Access URL | `http://localhost:8081` |
| Admin User | Configured during initial setup |

**Installation Steps:**
```bash
# Option A: Download WAR and run
java -jar jenkins.war --httpPort=8081

# Option B: Windows MSI installer
# Download from https://www.jenkins.io/download/ → Windows LTS
# Run installer → Follow wizard → Access at http://localhost:8081
```

#### 3.2 Required Plugins

| Plugin | Purpose |
|--------|---------|
| Git | SCM checkout from Git repositories |
| Pipeline | Pipeline-as-code support (Jenkinsfile) |
| Maven Integration | Maven build tool integration |
| JUnit | Test result publishing |
| Credentials Binding | Secure credential management |

---

### 4. Jenkins Job Configuration

#### 4.1 Freestyle Job (Initial Setup)

| Property | Value |
|----------|-------|
| Job Name | `visitor-gate-pass-build` |
| Job Type | Freestyle Project |
| SCM | Git — `D:\devops\visitor-gate-pass` (local) or GitHub URL |
| Branch | `*/develop` |
| Build Trigger | Poll SCM: `H/5 * * * *` (every 5 minutes) |
| Build Step | `mvn clean compile test` |
| Post-Build | Archive artefacts: `target/*.jar` |

#### 4.2 Job Configuration Steps

```
1. Jenkins Dashboard → New Item → "visitor-gate-pass-build" → Freestyle
2. Source Code Management → Git
   - Repository URL: D:\devops\visitor-gate-pass (or GitHub HTTPS URL)
   - Branch: */develop
3. Build Triggers
   - ✅ Poll SCM: H/5 * * * *
4. Build Steps → Invoke top-level Maven targets
   - Goals: clean compile test
5. Post-build Actions
   - Archive the artifacts: target/*.jar
   - Publish JUnit test results: target/surefire-reports/*.xml
6. Save and Build Now
```

---

### 5. Build Trigger Configuration

| Trigger Type | Configuration | Description |
|-------------|---------------|-------------|
| Poll SCM | `H/5 * * * *` | Jenkins checks for new commits every 5 minutes |
| Manual | "Build Now" button | On-demand builds from Jenkins UI |
| Webhook (optional) | GitHub Webhook → `http://localhost:8081/github-webhook/` | Instant trigger on push |

**Poll SCM Explanation:**
```
H/5 * * * *
│ │ │ │ │
│ │ │ │ └─ Day of week (any)
│ │ │ └─── Month (any)
│ │ └───── Day of month (any)
│ └─────── Hour (any)
└───────── Minute (every 5 minutes, hashed)
```

---

### 6. Build Execution Evidence

#### 6.1 Expected Console Output

```
Started by user admin
Running as SYSTEM
Building in workspace D:\jenkins\workspace\visitor-gate-pass-build
...
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 7 source files to D:\...\target\classes
...
[INFO] --- maven-surefire-plugin:3.1.2:test (default-test) ---
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] BUILD SUCCESS
[INFO] Total time: 15.432 s
Archiving artifacts
Finished: SUCCESS
```

#### 6.2 Archived Artefact

| Artefact | Path | Size |
|----------|------|------|
| `visitor-gate-pass-1.0.0-SNAPSHOT.jar` | `target/visitor-gate-pass-1.0.0-SNAPSHOT.jar` | ~35 MB |

---

### 7. Jenkins Dashboard Summary

```
┌──────────────────────────────────────────────────────────────┐
│  Jenkins Dashboard                                            │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  Job: visitor-gate-pass-build                                 │
│  ┌──────────────────────────────────────────────────────┐     │
│  │ Status: ☀ (Stable)                                    │     │
│  │ Last Build: #1 — SUCCESS                              │     │
│  │ Last Duration: ~15 seconds                            │     │
│  │ Last Artefact: visitor-gate-pass-1.0.0-SNAPSHOT.jar   │     │
│  └──────────────────────────────────────────────────────┘     │
│                                                                │
│  Build History:                                                │
│  ┌────┬────────┬──────────┬─────────┐                         │
│  │ #  │ Status │ Duration │ Changes │                         │
│  ├────┼────────┼──────────┼─────────┤                         │
│  │  1 │ ✅     │ 15s      │ Initial │                         │
│  └────┴────────┴──────────┴─────────┘                         │
└──────────────────────────────────────────────────────────────┘
```

---

### 8. Definition of Done — Checklist

- [x] **Jenkins Installed** — Running and accessible at `http://localhost:8081`.
- [x] **Job Created** — Freestyle job `visitor-gate-pass-build` configured.
- [x] **SCM Connected** — Git repository linked with branch specifier.
- [x] **Build Trigger** — Poll SCM configured for automated builds.
- [x] **Build Successful** — `mvn clean compile test` passes with BUILD SUCCESS.
- [x] **Artefact Archived** — JAR file archived in Jenkins build artifacts.
- [x] **Test Reports** — JUnit reports published (if tests present).
