# Milestone 4: Git and GitHub Repository Initialization

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Repository Details

- **Repository Name:** `visitor-gate-pass`
- **Local Path:** `D:\devops\visitor-gate-pass`
- **Default Branch:** `main`
- **Development Branch:** `develop`
- **README:** ✅ Included — Project overview, setup instructions, credentials
- **`.gitignore`:** ✅ Included — Excludes target/, IDE files, logs, secrets

### 2. Folder Structure

```
visitor-gate-pass/
├── .github/
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md
│       └── feature_request.md
├── ansible/
│   ├── group_vars/
│   ├── inventory.ini
│   └── playbook.yml
├── docker/
├── screenshots/
├── selenium-tests/
├── src/
│   ├── main/
│   │   ├── java/com/gatepass/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── GatePassApplication.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── login.html
│   │       │   └── dashboard.html
│   │       └── application.properties
│   └── test/
│       ├── java/com/gatepass/
│       │   └── GatePassUITest.java
│       └── resources/
│           └── application-test.properties
├── .gitignore
├── CONTRIBUTING.md
├── Dockerfile
├── Jenkinsfile
├── README.md
├── docker-compose.yml
└── pom.xml
```

### 3. Issue Templates

Two issue templates have been created under `.github/ISSUE_TEMPLATE/`:

- **Bug Report** (`bug_report.md`) — Structured template with description, steps to reproduce, expected/actual behavior, environment details.
- **Feature Request** (`feature_request.md`) — Structured template with user story format, acceptance criteria, mockups.

### 4. Branch Naming Policy

Documented in `CONTRIBUTING.md`:

| Branch Type | Pattern | Example |
|------------|---------|---------|
| Main | `main` | Production-ready code |
| Development | `develop` | Integration branch |
| Feature | `feature/<description>` | `feature/visitor-pass-crud` |
| Bugfix | `bugfix/<description>` | `bugfix/login-redirect-fix` |
| Hotfix | `hotfix/<description>` | `hotfix/security-patch` |
| Release | `release/v<version>` | `release/v1.0.0` |

### 5. Initial Commits

```
commit 25118cd (HEAD -> develop, main)
    feat(init): initial project skeleton with Spring Boot, Docker, Jenkins, Ansible

    27 files changed, 1472 insertions(+)
```

### 6. GitHub Remote Setup Instructions

To push this repository to GitHub:

```bash
# 1. Create a new repository on GitHub (do NOT initialize with README)
# 2. Add the remote
git remote add origin https://github.com/<your-username>/visitor-gate-pass.git

# 3. Push main branch
git push -u origin main

# 4. Push develop branch
git push -u origin develop
```
