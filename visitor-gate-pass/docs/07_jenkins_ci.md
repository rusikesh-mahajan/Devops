# Week 7: Jenkins Installation and Continuous Integration

**Project:** Automated Residential Visitor Gate-Pass System  
**Document Code:** DOC-W07-JCI  
**Version:** 1.0.0  
**Date:** October 14, 2026  

---

## 1. Jenkins Architecture & Environment Configuration

Jenkins is configured and running as a dedicated continuous integration service on the host machine:
- **Service Name:** `Jenkins`
- **Controller URL:** `http://localhost:8081`
- **Jenkins Home Directory:** `C:\ProgramData\Jenkins\.jenkins`
- **Executor Nodes:** Built-in node with 2 concurrent executors.
- **Java Runtime:** OpenJDK 21 LTS (`Temurin-21.0.8+9`).
- **Maven Integration:** Apache Maven 3.9.16 integrated on system PATH.

---

## 2. Installed & Configured Plugins

The following essential plugins are installed and active:
1. **Pipeline (`workflow-aggregator`):** Supports Declarative and Scripted Pipeline as Code.
2. **Git Plugin (`git`):** Connects to Git SCM repositories, supports branch tracking, polling, and webhooks.
3. **JUnit Plugin (`junit`):** Parses Surefire XML test reports and publishes interactive test trend charts.
4. **Docker Pipeline (`docker-workflow`):** Interacts with Docker daemon for image building, tagging, and registry management.
5. **Credentials Binding Plugin (`credentials-binding`):** Manages encrypted tokens, passwords, and SSH keys securely.

---

## 3. Jenkins CI Job: `visitor-gate-pass-pipeline`

A dedicated Pipeline project named `visitor-gate-pass-pipeline` is configured to build the repository.

### Configuration Details:
- **Definition:** Pipeline script from SCM.
- **SCM:** Git.
- **Script Path:** `Jenkinsfile`.
- **Lightweight Checkout:** Enabled.
- **Trigger:** Poll SCM (`H/5 * * * *` - checks for new commits every 5 minutes) and manual dispatch via `buildWithParameters`.

---

## 4. Operational Verification

### Service Verification Command:
```powershell
Get-Service -Name "*jenkins*"
# Output:
# Status   Name      DisplayName
# ------   ----      -----------
# Running  Jenkins   Jenkins
```

### Dashboard Endpoint Verification:
```powershell
Invoke-WebRequest -Uri "http://localhost:8081" -UseBasicParsing
# Returns HTTP 200 OK with Jenkins UI
```

### Job Query via REST API:
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/job/visitor-gate-pass-pipeline/api/json"
# Returns job metadata, build history (Build #1 through #7), and status: BLUE (SUCCESS)
```
