# Feature Specification: Visitor Pass Core CRUD & Validation (Week 5)

**Feature Code:** FEAT-W05-VPCRUD  
**Branch:** `feature/visitor-pass-crud`  
**Target:** `develop`  

---

## 1. Feature Summary
Implements end-to-end visitor pass registration at the front gate, including client/server-side validation, persistent relational mapping with resident hosts, and automated pass code generation (`GP-XXXX`).

---

## 2. Components Created & Tested
1. **Entity Layer (`VisitorPass.java`):**
   - Validation constraints: `@NotBlank`, `@Size`, `@Pattern`.
   - Lifecycle hook: `@PrePersist` generating UUID-based human readable code (`GP-XXXX`) and timestamp.
2. **Repository Layer (`VisitorPassRepository.java`):**
   - Status filtering (`findByStatus`).
   - Tenant scoping (`findByHostId`).
   - Multi-field text search (`searchAllFields`).
3. **Controller & View Layer (`WebController.java` & `dashboard.html`):**
   - Gate Guard visitor registration form with resident dropdown selector.
   - Flash attributes for user notification on successful pass creation.
4. **Automated Verification:**
   - Unit tests: `VisitorPassRepositoryTest` (4/4 passed).
   - MockMvc integration: `WebControllerTest.testGuardCreatePassSuccess` (passed).
   - Headless Chrome UI: `GatePassUITest.testGuardCreatePassWorkflow` (passed).
