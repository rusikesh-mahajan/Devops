# Contributing to Automated Residential Visitor Gate-Pass System

Thank you for contributing to the **Automated Residential Visitor Gate-Pass System**! To maintain code quality, reliable CI/CD automation, and clean Git history across our 15-week DevOps lifecycle, all team members and contributors must adhere to the policies outlined below.

---

## 1. GitFlow Branching Strategy

Our repository strictly follows the **Gitflow** branching model:

```
main (Production Releases, Tagged v1.0.0)
  ▲
  │  (Merge via Release PR)
develop (Sprint Integration Baseline)
  ▲
  ├───────► feature/visitor-pass-crud (Feature 1)
  ├───────► feature/resident-workflow-analytics (Feature 2)
  └───────► hotfix/* (Emergency Production Fixes)
```

### Branch Responsibilities
- **`main`**: Protected production branch. Contains tagged, verified release baselines (`v1.0.0`). Direct pushes are prohibited.
- **`develop`**: Integration branch. Serves as the continuous integration baseline where all completed feature branches are merged.
- **`feature/*`**: Temporary feature branches created off `develop`. Used for isolated user story development.
- **`hotfix/*`**: Emergency patch branches created off `main` to address critical production issues.

---

## 2. Branch Naming Policy

Branches must follow the standardized naming convention:
- **Feature Branches**: `feature/<module-or-ticket-name>`  
  *Examples:* `feature/visitor-pass-crud`, `feature/resident-workflow-analytics`, `feature/docker-ci-pipeline`
- **Bugfix Branches**: `bugfix/<issue-id>-<description>`  
  *Example:* `bugfix/issue-12-csrf-token-expiration`
- **Hotfix Branches**: `hotfix/<patch-version>`  
  *Example:* `hotfix/v1.0.1`

---

## 3. Commit Message Guidelines

All commit messages must adhere to the **Conventional Commits** specification:

```
<type>(<scope>): <short description in present tense>

[optional body explaining motivation / context]

[optional footer referencing issue numbers: Fixes #123]
```

### Allowed Types
- **`feat`**: A new feature for the user or workflow
- **`fix`**: A bug fix in existing code
- **`docs`**: Documentation changes only (Markdown, reports)
- **`test`**: Adding missing tests or correcting existing tests
- **`ci`**: Changes to CI/CD pipelines (Jenkinsfile, Docker Compose, Ansible)
- **`refactor`**: Code change that neither fixes a bug nor adds a feature
- **`chore`**: Maintenance, dependency updates, build tooling adjustments

*Examples:*
- `feat(visitor): add form validation and auto pass code generation`
- `fix(ui): enhance form input placeholder and table text contrast for dark theme`
- `ci(jenkins): add quality gate halting pipeline on test failure`

---

## 4. Pull Request & Review Policy

1. **Branch Off `develop`**: Always branch from the latest `develop` branch (`git checkout -b feature/<name> develop`).
2. **Local Quality Verification**: Prior to raising a Pull Request, verify:
   ```powershell
   # 1. Clean build & unit/integration tests
   mvn clean test -Dtest=VisitorPassRepositoryTest,WebControllerTest

   # 2. Selenium headless browser journeys
   mvn test -Dtest=GatePassUITest

   # 3. Docker build test
   docker build -t visitor-gate-pass:local-test .
   ```
3. **Pull Request Submission**: Fill out the PR template (`.github/PULL_REQUEST_TEMPLATE.md`) with testing evidence.
4. **Merge Strategy**: Feature branches are merged into `develop` using non-fast-forward merge commits (`git merge --no-ff`) to preserve the branching history in Git graph visualizations.

---

## 5. Definition of Done (DoD)

A user story or feature is considered **Done** only when:
- [x] Code compiles without errors and passes all compiler static analysis checks.
- [x] Unit and integration tests pass with 100% success rate.
- [x] Headless Selenium E2E UI tests pass and screenshot evidence is archived.
- [x] Jenkins CI pipeline runs to completion (`SUCCESS`) and builds Docker image.
- [x] Code is merged into `develop` via Pull Request with peer review comments.
