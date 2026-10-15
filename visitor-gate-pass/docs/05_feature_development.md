# Week 5: Feature Development with Branching

**Project:** Automated Residential Visitor Gate-Pass System  
**Document Code:** DOC-W05-FDB  
**Version:** 1.0.0  
**Date:** October 14, 2026  

---

## 1. Feature Branch Workflow: Visitor Pass CRUD

The first core workflow implemented the end-to-end creation, validation, persistence, and presentation of visitor gate-passes on the isolated branch `feature/visitor-pass-crud`.

### Branching Commands Executed:
```bash
# Create and switch to feature branch
git checkout develop
git checkout -b feature/visitor-pass-crud

# Development cycle: Edit entity, repository, controller, and templates
git status
git add src/main/java/com/gatepass/entity/VisitorPass.java
git commit -m "feat(validation): add validation annotations to VisitorPass entity"

# Verification
mvn test
```

---

## 2. Technical Implementation Highlights

1. **Entity Definition (`VisitorPass.java`):**
   - Implemented validation annotations (`@NotBlank`, `@Size(min = 2, max = 100)`, `@Pattern` for phone).
   - Added automatic timestamping (`@PrePersist` setting `createdAt = LocalDateTime.now()`).
   - Added unique human-readable pass code generation (`GP-XXXX`).
2. **Persistence Layer (`VisitorPassRepository.java`):**
   - Extended `JpaRepository<VisitorPass, Long>`.
   - Implemented custom derived query methods (`findByStatus`, `findByHostId`, `searchAllFields`).
3. **Web Controller Endpoint (`/guard/create-pass`):**
   - Intercepted incoming form parameters.
   - Enforced server-side validation against blank names and invalid phone regex patterns.
   - Associated record with foreign-key `host` entity.
   - Dispatched flash attributes (`redirectAttributes.addFlashAttribute`) for UI confirmation.

---

## 3. Pull Request Review Checklist

Prior to merging `feature/visitor-pass-crud` into `develop`:
- [x] Code adheres to Java naming conventions and clean architecture separation.
- [x] Input validation enforced both in client HTML and server controller.
- [x] Foreign key relationships defined with proper JPA `@ManyToOne` mapping.
- [x] Pass code generated deterministically and formatted properly (`GP-XXXX`).
- [x] All automated unit and UI tests passing with zero regressions.
- [x] No plaintext passwords or credentials checked into source control.

---

## 4. Verification Evidence

Direct Selenium UI test `testGuardCreatePassWorkflow()` confirmed:
1. User logs in as `guard1`.
2. Form fields populated: "Bruce Wayne", "+1 555-010-9999", "BAT-01", "Executive Security Audit", Host: "John Doe".
3. Form submitted via POST.
4. Database persisted record with status `PENDING` and assigned pass code `GP-8172`.
5. Dashboard displayed green flash confirmation alert and appended new row to records table.
