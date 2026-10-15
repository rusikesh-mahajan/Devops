# Week 9: Selenium Test Design and Local Execution

**Project:** Automated Residential Visitor Gate-Pass System  
**Document Code:** DOC-W09-STD  
**Version:** 1.0.0  
**Date:** October 14, 2026  

---

## 1. Selenium Test Strategy & Architecture

End-to-end browser verification is implemented in `src/test/java/com/gatepass/GatePassUITest.java` utilizing **Selenium WebDriver 4.19.1** and **JUnit 5**.

### Key Architectural Principles:
1. **Dynamic Port Binding:** Runs against Spring Boot test server on random port (`webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT`) avoiding port collisions with host services.
2. **Headless Chrome Execution:** Configured with `--headless`, `--no-sandbox`, `--disable-gpu`, and explicit window size `1920x1080` for reliable execution across developer laptops, headless CI nodes, and Jenkins slaves.
3. **Automated Driver Management:** Leverages Selenium 4 Manager to automatically acquire compatible ChromeDriver matching the local Chrome version (`154.0.8037.99`). No machine-specific driver paths are hardcoded.
4. **Explicit Synchronization:** Employs `WebDriverWait` with `ExpectedConditions` (e.g., `visibilityOfElementLocated`, `presenceOfElementLocated`) avoiding flaky thread sleeps.
5. **Automated Evidence Capture:** `tearDown(TestInfo)` automatically captures full-page PNG screenshots upon test completion and stores them in `screenshots/`.

---

## 2. Five Critical User Journeys Tested

### Journey 1: Admin Login and Dashboard Metrics Display
- **Procedure:** Log in with `admin` / `admin123`.
- **Assertions:** Verify `#stats-section` renders, stat labels (`Total Passes`, `Pending`, `Inside (Active)`) are visible, and `#stat-total` displays count.
- **Evidence File:** `screenshots/Journey_1_Admin_Login_and_Dashboard_Metrics_Display.png`

### Journey 2: Guard Gate-Pass Creation Workflow
- **Procedure:** Log in with `guard1` / `guard123`. Locate `#create-pass-form`. Fill visitorName ("Bruce Wayne"), phone ("+1 555-010-9999"), vehicleNo ("BAT-01"), purpose ("Executive Security Audit"), and select host resident. Submit form.
- **Assertions:** Verify redirected to dashboard with newly generated row in `#records-table` containing "Bruce Wayne" and pass code `GP-XXXX`.
- **Evidence File:** `screenshots/Journey_2_Guard_Gate_Pass_Creation_Workflow.png`

### Journey 3: Resident Approval and Rejection Flow
- **Procedure:** Log in with `resident1` / `res123`.
- **Assertions:** Verify that `#records-table` contains passes associated with the resident's apartment. Verify approval/rejection action buttons.
- **Evidence File:** `screenshots/Journey_3_Resident_Approval_and_Rejection_Flow.png`

### Journey 4: Guard Check-In and Check-Out Operations
- **Procedure:** Log in with `guard1` / `guard123`. Locate table of active passes.
- **Assertions:** Verify table visibility and verify Check-In / Check-Out buttons are displayed for approved and checked-in passes.
- **Evidence File:** `screenshots/Journey_4_Guard_Check_In_and_Check_Out_Operations.png`

### Journey 5: Admin Search and Filtering
- **Procedure:** Log in with `admin` / `admin123`. Locate `#search-input`, type "Robert", and click `#search-btn`.
- **Assertions:** Verify table filters immediately to display "Robert Johnson" (seeded pass `GP-1001`).
- **Evidence File:** `screenshots/Journey_5_Admin_Search_and_Filtering.png`

---

## 3. Test Execution Results

```text
[INFO] Running com.gatepass.GatePassUITest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 14.86 s -- in com.gatepass.GatePassUITest
[INFO] Results:
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
- **Total Test Cases:** 5
- **Passed:** 5 (100%)
- **Failed:** 0
- **Screenshots Generated:** 5 high-resolution PNG evidence files in `screenshots/`.
