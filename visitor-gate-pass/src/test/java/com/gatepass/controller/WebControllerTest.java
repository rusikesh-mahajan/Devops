package com.gatepass.controller;

import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
import com.gatepass.repository.UserRepository;
import com.gatepass.repository.VisitorPassRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VisitorPassRepository passRepository;

    private User testResident;

    @BeforeEach
    public void setup() {
        passRepository.deleteAll();
        testResident = userRepository.findByUsername("resident1").orElse(null);
        if (testResident == null) {
            testResident = userRepository.save(new User("resident1", "$2a$10$dummy", "ROLE_RESIDENT", "John Doe"));
        }
    }

    @Test
    @DisplayName("Unauthenticated request to /dashboard should redirect to /login")
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Login page should be publicly accessible")
    public void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin dashboard should return 200 with metrics attributes")
    public void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("role", "totalPasses", "pendingPasses", "checkedInPasses"));
    }

    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Guard should successfully create a valid visitor pass")
    public void testGuardCreatePassSuccess() throws Exception {
        mockMvc.perform(post("/guard/create-pass")
                        .param("visitorName", "Carlos Sainz")
                        .param("phone", "+1 555-432-1098")
                        .param("purpose", "Delivery")
                        .param("vehicleNo", "FERRARI-55")
                        .param("hostId", testResident.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        Assertions.assertEquals(1, passRepository.findAll().size());
        VisitorPass saved = passRepository.findAll().get(0);
        Assertions.assertEquals("Carlos Sainz", saved.getVisitorName());
        Assertions.assertEquals("PENDING", saved.getStatus());
        Assertions.assertNotNull(saved.getPassCode());
    }

    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Guard create pass should reject invalid phone number")
    public void testGuardCreatePassValidationFailure() throws Exception {
        mockMvc.perform(post("/guard/create-pass")
                        .param("visitorName", "Bob")
                        .param("phone", "abc") // invalid phone
                        .param("purpose", "Test")
                        .param("hostId", testResident.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        Assertions.assertEquals(0, passRepository.count());
    }

    @Test
    @WithMockUser(username = "resident1", roles = {"RESIDENT"})
    @DisplayName("Resident should be able to approve a PENDING pass for their apartment")
    public void testResidentApprovePass() throws Exception {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Lando Norris");
        pass.setPhone("+1 555-444-4444");
        pass.setPurpose("Friend Visit");
        pass.setStatus("PENDING");
        pass.setHost(testResident);
        pass = passRepository.save(pass);

        mockMvc.perform(post("/resident/update-status/" + pass.getId())
                        .param("status", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        VisitorPass updated = passRepository.findById(pass.getId()).orElseThrow();
        Assertions.assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("State Machine: Guard CANNOT check in a PENDING pass directly")
    public void testCannotCheckInPendingPass() throws Exception {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Oscar Piastri");
        pass.setPhone("+1 555-888-8888");
        pass.setPurpose("Guest");
        pass.setStatus("PENDING");
        pass.setHost(testResident);
        pass = passRepository.save(pass);

        mockMvc.perform(post("/guard/update-status/" + pass.getId())
                        .param("status", "CHECKED_IN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        VisitorPass unmodified = passRepository.findById(pass.getId()).orElseThrow();
        Assertions.assertEquals("PENDING", unmodified.getStatus());
    }

    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("State Machine: Guard CAN check in an APPROVED pass and check out a CHECKED_IN pass")
    public void testGuardCheckInAndCheckOut() throws Exception {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Charles Leclerc");
        pass.setPhone("+1 555-161-6161");
        pass.setPurpose("VIP Guest");
        pass.setStatus("APPROVED");
        pass.setHost(testResident);
        pass = passRepository.save(pass);

        // Step 1: Check In
        mockMvc.perform(post("/guard/update-status/" + pass.getId())
                        .param("status", "CHECKED_IN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        VisitorPass checkedIn = passRepository.findById(pass.getId()).orElseThrow();
        Assertions.assertEquals("CHECKED_IN", checkedIn.getStatus());
        Assertions.assertNotNull(checkedIn.getCheckInTime());

        // Step 2: Check Out
        mockMvc.perform(post("/guard/update-status/" + pass.getId())
                        .param("status", "CHECKED_OUT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        VisitorPass checkedOut = passRepository.findById(pass.getId()).orElseThrow();
        Assertions.assertEquals("CHECKED_OUT", checkedOut.getStatus());
        Assertions.assertNotNull(checkedOut.getCheckOutTime());
    }

    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Pass Details API should return JSON with all fields")
    public void testPassDetailsApi() throws Exception {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Max Verstappen");
        pass.setPhone("+1 555-101-0101");
        pass.setPurpose("Racing Simulator Delivery");
        pass.setVehicleNo("RED-BULL-1");
        pass.setStatus("PENDING");
        pass.setHost(testResident);
        pass = passRepository.save(pass);

        mockMvc.perform(get("/api/passes/" + pass.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitorName").value("Max Verstappen"))
                .andExpect(jsonPath("$.phone").value("+1 555-101-0101"))
                .andExpect(jsonPath("$.vehicleNo").value("RED-BULL-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
