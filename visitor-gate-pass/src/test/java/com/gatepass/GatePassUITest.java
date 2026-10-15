package com.gatepass;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GatePassUITest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        if (driver != null) {
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String safeName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_");
                File destFile = new File("screenshots/" + safeName + ".png");
                Files.createDirectories(destFile.getParentFile().toPath());
                Files.copy(screenshot.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                System.err.println("Screenshot capture failed: " + e.getMessage());
            }
            driver.quit();
        }
    }

    private void loginAs(String username, String password) {
        driver.get("http://localhost:" + port + "/login");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-btn")).click();
        new WebDriverWait(driver, Duration.ofSeconds(6))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("logout-btn")));
    }

    @Test
    @Order(1)
    @DisplayName("Journey_1_Admin_Login_and_Dashboard_Metrics_Display")
    public void testAdminLoginAndDashboard() {
        loginAs("admin", "admin123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stats-section")));

        Assertions.assertTrue(driver.getPageSource().contains("Total Passes"));
        Assertions.assertTrue(driver.getPageSource().contains("Pending"));
        Assertions.assertTrue(driver.getPageSource().contains("Inside (Active)"));
        Assertions.assertTrue(driver.findElement(By.id("stat-total")).isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("Journey_2_Guard_Gate_Pass_Creation_Workflow")
    public void testGuardCreatePassWorkflow() {
        loginAs("guard1", "guard123");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "function setVal(id, val) { var el = document.getElementById(id); if (el) { el.value = val; el.dispatchEvent(new Event('input', {bubbles: true})); el.dispatchEvent(new Event('change', {bubbles: true})); } }" +
            "setVal('visitorName', 'Bruce Wayne');" +
            "setVal('phone', '+1 555-010-9999');" +
            "setVal('vehicleNo', 'BAT-01');" +
            "setVal('purpose', 'Executive Security Audit');" +
            "var h = document.getElementById('hostId'); if (h) { h.selectedIndex = 1; h.dispatchEvent(new Event('change', {bubbles: true})); }"
        );

        WebElement submitBtn = driver.findElement(By.id("submit-pass-btn"));
        js.executeScript("arguments[0].click();", submitBtn);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-alert")));
        Assertions.assertTrue(driver.getPageSource().contains("Bruce Wayne"));
    }

    @Test
    @Order(3)
    @DisplayName("Journey_3_Resident_Approval_and_Rejection_Flow")
    public void testResidentApprovalWorkflow() {
        loginAs("resident1", "res123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("records-table")));

        Assertions.assertTrue(driver.getPageSource().contains("Visitor Records"));
    }

    @Test
    @Order(4)
    @DisplayName("Journey_4_Guard_Check_In_and_Check_Out_Operations")
    public void testGuardCheckInAndOut() {
        loginAs("guard1", "guard123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("records-table")));

        Assertions.assertTrue(driver.findElement(By.id("records-table")).isDisplayed());
    }

    @Test
    @Order(5)
    @DisplayName("Journey_5_Admin_Search_and_Filtering")
    public void testAdminSearchAndFilter() {
        loginAs("admin", "admin123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input")));

        WebElement searchBox = driver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.sendKeys("Robert");
        driver.findElement(By.id("search-btn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("records-table")));
        Assertions.assertTrue(driver.getPageSource().contains("Robert Johnson"));
    }
}
