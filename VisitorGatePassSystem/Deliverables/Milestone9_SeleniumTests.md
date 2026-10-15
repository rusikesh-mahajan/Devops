# Milestone 9: Selenium Tests

## Project: Automated Residential Visitor Gate-Pass System

---

### 1. Sprint Goal

Develop automated UI tests using Selenium WebDriver to ensure the core workflows (Login, Dashboard, Pass Creation) function correctly. This acts as a foundation for integrating automated acceptance testing into the CI pipeline. This addresses the first part of **US-13**.

---

### 2. User Story Addressed

| ID | User Story | Priority | Status |
|----|-----------|----------|--------|
| US-13 | As a **DevOps Engineer**, I want Selenium tests in the pipeline so that UI regressions are caught before deployment. | LOW | 🔄 In Progress (Test Script Creation) |

---

### 3. Selenium Test Plan

**Scope:**
Test the critical paths of the Visitor Gate-Pass web application.

**Test Cases:**
1. **TC-01: Admin Login and Dashboard View**
   - Goal: Verify admin can login and see the dashboard with stats.
2. **TC-02: Guard Create Pass Workflow**
   - Goal: Verify guard can login and access the "Create New Visitor Pass" form.
3. **TC-03: Resident View Dashboard**
   - Goal: Verify resident can login and see the dashboard without admin stats.
4. **TC-04: Invalid Login Handling**
   - Goal: Verify invalid credentials display an error message.
5. **TC-05: Logout Workflow**
   - Goal: Verify logging out redirects to the login page successfully.

**Tools & Dependencies:**
- Java 17
- JUnit 5 (`org.junit.jupiter`)
- Selenium WebDriver (`org.seleniumhq.selenium:selenium-java:4.19.1`)
- Spring Boot Test (`@SpringBootTest`) for spinning up a local server on a random port.

---

### 4. Implementation Details

**Location:** `src/test/java/com/gatepass/GatePassUITest.java`

#### 4.1 WebDriver Setup and Teardown

The tests are configured to run in **headless mode** to ensure they can run successfully within a CI/CD environment (like Jenkins) without a graphical display.

```java
@BeforeEach
public void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // Crucial for CI/CD
    options.addArguments("--remote-allow-origins=*");
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
}

@AfterEach
public void tearDown(TestInfo testInfo) {
    if (driver != null) {
        // Screenshot capture implementation
        driver.quit();
    }
}
```

#### 4.2 Test Implementations

**TC-01: Admin Login and Dashboard (Implemented)**
```java
@Test
@Order(1)
public void testAdminLoginAndDashboard() {
    driver.get("http://localhost:" + port + "/login");
    driver.findElement(By.id("username")).sendKeys("admin");
    driver.findElement(By.id("password")).sendKeys("admin123");
    driver.findElement(By.tagName("button")).click();

    new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Admin Dashboard')]")));

    Assertions.assertTrue(driver.getPageSource().contains("Admin Dashboard"));
    Assertions.assertTrue(driver.getPageSource().contains("Total Passes"));
}
```

**TC-02: Guard Create Pass Workflow (Implemented)**
```java
@Test
@Order(2)
public void testGuardCreatePassWorkflow() {
    driver.get("http://localhost:" + port + "/login");
    driver.findElement(By.id("username")).sendKeys("guard1");
    driver.findElement(By.id("password")).sendKeys("guard123");
    driver.findElement(By.tagName("button")).click();

    new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Create New Visitor Pass')]")));
    
    Assertions.assertTrue(driver.getPageSource().contains("Create New Visitor Pass"));
    Assertions.assertNotNull(driver.findElement(By.name("visitorName")));
}
```

---

### 5. Local Execution and Reporting

**Command to run tests locally:**
```bash
mvn test -Dtest=GatePassUITest
```

**Local Test Report:**
Maven Surefire plugin generates reports in `target/surefire-reports/`.
The output XML files contain the test results, duration, and any errors.

**Screenshot on Failure Setup:**
The `@AfterEach` block is configured to take a screenshot of the browser window. In a real-world scenario, this would be tied to test failure evaluation. For this MVP, it takes screenshots for reference.

```java
File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
File destFile = new File("screenshots/" + testInfo.getDisplayName() + ".png");
```

---

### 6. Definition of Done — Checklist

- [x] **Selenium Dependency Added** — Added `selenium-java` to `pom.xml`.
- [x] **Test Class Created** — `GatePassUITest.java` created in `src/test`.
- [x] **Headless Mode Configured** — WebDriver configured for headless execution.
- [x] **Test Cases Written** — Tests for Admin and Guard workflows implemented.
- [x] **Local Execution Successful** — Tests pass when run via Maven locally.
- [x] **Screenshot Capture** — Logic implemented to save screenshots.
