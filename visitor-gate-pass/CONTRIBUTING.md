# Contributing to Visitor Gate-Pass System

## Branch Naming Convention

| Branch Type | Pattern | Example |
|------------|---------|---------|
| Main/Production | `main` | `main` |
| Development | `develop` | `develop` |
| Feature | `feature/<short-description>` | `feature/visitor-pass-crud` |
| Bugfix | `bugfix/<short-description>` | `bugfix/login-redirect-fix` |
| Hotfix | `hotfix/<short-description>` | `hotfix/security-patch` |
| Release | `release/v<version>` | `release/v1.0.0` |

## Commit Message Convention

Use clear, descriptive commit messages following this format:

```
<type>(<scope>): <short description>

[optional body]
[optional footer]
```

**Types:**
- `feat` — A new feature
- `fix` — A bug fix
- `docs` — Documentation changes
- `style` — Code style changes (formatting, no logic change)
- `refactor` — Code restructuring without changing behavior
- `test` — Adding or updating tests
- `chore` — Build process or tooling changes

**Examples:**
```
feat(guard): add visitor pass creation form
fix(auth): resolve login redirect loop
docs(readme): update setup instructions
test(selenium): add resident approval workflow test
chore(docker): update Dockerfile base image
```

## Workflow

1. Create a feature branch from `develop`: `git checkout -b feature/my-feature develop`
2. Make your changes and commit with meaningful messages.
3. Push to remote: `git push origin feature/my-feature`
4. Create a Pull Request targeting `develop`.
5. Request a code review.
6. After approval, merge and delete the feature branch.

## Pull Request Guidelines

- PRs must target the `develop` branch (not `main`).
- Include a description of what was changed and why.
- Reference related GitHub issues (e.g., "Closes #5").
- Ensure all tests pass before requesting review.
- At least one approval is required before merging.

## Code Standards

- Java 17 syntax and conventions.
- Follow Spring Boot best practices.
- All public methods should have meaningful names.
- Keep controllers thin — business logic in services.
- Write tests for new features.
