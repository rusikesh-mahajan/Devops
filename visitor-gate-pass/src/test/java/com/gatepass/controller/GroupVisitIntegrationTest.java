package com.gatepass.controller;

import com.gatepass.entity.GroupVisit;
import com.gatepass.entity.User;
import com.gatepass.repository.GroupVisitRepository;
import com.gatepass.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Residential Visitor Gate-Pass System: RBAC & Group Visit Integration Tests (12 Required Scenarios)")
public class GroupVisitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupVisitRepository groupVisitRepository;

    private User resident1;
    private User resident2;

    @BeforeEach
    public void setup() {
        groupVisitRepository.deleteAll();

        resident1 = userRepository.findByUsername("resident1").orElseGet(() -> {
            User u = new User("resident1", "$2a$10$dummy", "ROLE_RESIDENT", "John Resident");
            u.setUnitNumber("B-204");
            u.setPhone("+1 555-010-0001");
            return userRepository.save(u);
        });

        resident2 = userRepository.findByUsername("resident2").orElseGet(() -> {
            User u = new User("resident2", "$2a$10$dummy", "ROLE_RESIDENT", "Alice Host");
            u.setUnitNumber("A-105");
            u.setPhone("+1 555-010-0002");
            return userRepository.save(u);
        });
    }

    // =========================================================================
    // Scenario 1: Admin accesses admin dashboard (200 OK, admin-dashboard view)
    // =========================================================================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Scenario 1: Admin successfully accesses Admin Dashboard with system-wide analytics and user management")
    public void testAdminAccessesAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(model().attributeExists("totalVisitorsInside", "totalGroupsInside", "allUsers", "unitsDirectory"));
    }

    // =========================================================================
    // Scenario 2: Guard accessing admin dashboard is denied (403 Forbidden)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 2: Guard attempting to access Admin Dashboard is denied (403 Forbidden)")
    public void testGuardAccessingAdminDashboardDenied() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Scenario 3: Guard accesses guard dashboard (200 OK, guard-dashboard view)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 3: Guard successfully accesses Guard Gate Operations Console")
    public void testGuardAccessesGuardDashboard() throws Exception {
        mockMvc.perform(get("/guard/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("guard-dashboard"))
                .andExpect(model().attributeExists("groupVisits", "passes", "residentsList", "totalGuestsInsidePremises"));
    }

    // =========================================================================
    // Scenario 4: Guard registers group visit (5 total -> 4 accompanying)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 4: Guard registers group visit with 5 guests (Rahul Sharma, 4 accompanying) -> unique GV code generated")
    public void testGuardRegistersGroupVisit() throws Exception {
        mockMvc.perform(post("/guard/create-group-visit")
                        .with(csrf())
                        .param("primaryVisitorName", "Rahul Sharma")
                        .param("phone", "+91 98765 43210")
                        .param("hostId", resident1.getId().toString())
                        .param("flatNumber", "B-204")
                        .param("totalGuests", "5")
                        .param("accompanyingGuestNames", "Neha Sharma, Rohan Sharma, Amit Sharma, Sneha Sharma")
                        .param("purpose", "Family Dinner")
                        .param("vehicleNo", "MH-12-AB-1234")
                        .param("expectedArrivalTime", LocalDateTime.now().plusHours(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        List<GroupVisit> list = groupVisitRepository.findAll();
        assertEquals(1, list.size());
        GroupVisit created = list.get(0);
        assertEquals("Rahul Sharma", created.getPrimaryVisitorName());
        assertEquals(5, created.getTotalGuests());
        assertEquals(4, created.getAccompanyingGuests(), "Accompanying guests must strictly be 4 (5 - 1)");
        assertEquals("B-204", created.getFlatNumber());
        assertEquals("PENDING", created.getStatus());
        assertTrue(created.getGroupCode().startsWith("GV-"), "Group code must start with GV- prefix");
    }

    // =========================================================================
    // Scenario 5: Resident approves pending group visit
    // =========================================================================
    @Test
    @WithMockUser(username = "resident1", roles = {"RESIDENT"})
    @DisplayName("Scenario 5: Resident approves pending group visit directed to their flat")
    public void testResidentApprovesGroupVisit() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Vikram Malhotra");
        gv.setPhone("+91 98765 11111");
        gv.setHost(resident1);
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(3);
        gv.setPurpose("Colleague Meeting");
        gv.setStatus("PENDING");
        gv = groupVisitRepository.save(gv);

        mockMvc.perform(post("/resident/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        GroupVisit updated = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("APPROVED", updated.getStatus());
    }

    // =========================================================================
    // Scenario 6: Guard checks in approved group visit (active inside increases)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 6: Guard checks in APPROVED group visit -> status CHECKED_IN, timestamp recorded")
    public void testGuardChecksInApprovedGroupVisit() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Ananya Verma");
        gv.setPhone("+91 98765 22222");
        gv.setHost(resident1);
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(4);
        gv.setPurpose("Project Discussion");
        gv.setStatus("APPROVED");
        gv = groupVisitRepository.save(gv);

        mockMvc.perform(post("/guard/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "CHECKED_IN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        GroupVisit checkedIn = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("CHECKED_IN", checkedIn.getStatus());
        assertNotNull(checkedIn.getCheckInTime(), "Check-in time must be recorded");

        // Verify active inside calculation from database
        Long activeInsideGuests = groupVisitRepository.sumActiveCheckedInGuests();
        assertEquals(4L, activeInsideGuests, "Active guests inside premises must reflect the checked-in group size");
    }

    // =========================================================================
    // Scenario 7: Duplicate check-in is rejected
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 7: Duplicate check-in attempt on already CHECKED_IN group is rejected with error")
    public void testDuplicateCheckInRejected() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Karan Mehra");
        gv.setPhone("+91 98765 33333");
        gv.setHost(resident1);
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(2);
        gv.setPurpose("Consultation");
        gv.setStatus("CHECKED_IN");
        LocalDateTime initialCheckIn = LocalDateTime.now().minusMinutes(30);
        gv.setCheckInTime(initialCheckIn);
        gv = groupVisitRepository.save(gv);

        mockMvc.perform(post("/guard/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "CHECKED_IN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        GroupVisit afterAttempt = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("CHECKED_IN", afterAttempt.getStatus());
        assertEquals(initialCheckIn.toLocalDate(), afterAttempt.getCheckInTime().toLocalDate());
    }

    // =========================================================================
    // Scenario 8: Rejected group visit cannot be checked in
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 8: Guard CANNOT check in a REJECTED group visit")
    public void testRejectedGroupCannotBeCheckedIn() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Siddharth Roy");
        gv.setPhone("+91 98765 44444");
        gv.setHost(resident1);
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(3);
        gv.setPurpose("Unsolicited Sales");
        gv.setStatus("REJECTED");
        gv = groupVisitRepository.save(gv);

        mockMvc.perform(post("/guard/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "CHECKED_IN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        GroupVisit stillRejected = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("REJECTED", stillRejected.getStatus());
        assertNull(stillRejected.getCheckInTime(), "Check-in time must remain null");
    }

    // =========================================================================
    // Scenario 9: Guard checks out group visit (active inside count decreases)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 9: Guard checks out group visit -> status CHECKED_OUT, timestamp recorded, active count drops")
    public void testGuardChecksOutGroupVisit() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Divya Joshi");
        gv.setPhone("+91 98765 55555");
        gv.setHost(resident1);
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(6);
        gv.setPurpose("Birthday Party");
        gv.setStatus("CHECKED_IN");
        gv.setCheckInTime(LocalDateTime.now().minusHours(3));
        gv = groupVisitRepository.save(gv);

        // Before check-out: 6 guests active inside
        assertEquals(6L, groupVisitRepository.sumActiveCheckedInGuests());

        mockMvc.perform(post("/guard/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "CHECKED_OUT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        GroupVisit checkedOut = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("CHECKED_OUT", checkedOut.getStatus());
        assertNotNull(checkedOut.getCheckOutTime(), "Check-out time must be recorded");

        // After check-out: 0 guests active inside
        Long insideAfter = groupVisitRepository.sumActiveCheckedInGuests();
        assertEquals(0L, insideAfter == null ? 0L : insideAfter, "Active inside guest count must drop to 0 after checkout");
    }

    // =========================================================================
    // Scenario 10: Resident cannot approve/reject visit for another resident's flat
    // =========================================================================
    @Test
    @WithMockUser(username = "resident2", roles = {"RESIDENT"})
    @DisplayName("Scenario 10: Cross-Resident Security: resident2 CANNOT approve resident1's group visit")
    public void testCrossResidentApprovalBlocked() throws Exception {
        GroupVisit gv = new GroupVisit();
        gv.setPrimaryVisitorName("Target Guest");
        gv.setPhone("+91 98765 66666");
        gv.setHost(resident1); // resident1's guest
        gv.setFlatNumber("B-204");
        gv.setTotalGuests(2);
        gv.setPurpose("Private Visit");
        gv.setStatus("PENDING");
        gv = groupVisitRepository.save(gv);

        // resident2 tries to approve resident1's visitor
        mockMvc.perform(post("/resident/group-status/" + gv.getId())
                        .with(csrf())
                        .param("status", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        GroupVisit stillPending = groupVisitRepository.findById(gv.getId()).orElseThrow();
        assertEquals("PENDING", stillPending.getStatus(), "Visit status must not change when unauthorized resident attempts approval");
    }

    // =========================================================================
    // Scenario 11: Group search returns matching records
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 11: Group search filters matching records by visitor name, flat, phone, or group code")
    public void testGroupSearchFiltersCorrectly() throws Exception {
        GroupVisit gv1 = new GroupVisit();
        gv1.setPrimaryVisitorName("Aarav Gupta");
        gv1.setPhone("+91 98765 77771");
        gv1.setHost(resident1);
        gv1.setFlatNumber("B-204");
        gv1.setTotalGuests(3);
        gv1.setStatus("APPROVED");
        groupVisitRepository.save(gv1);

        GroupVisit gv2 = new GroupVisit();
        gv2.setPrimaryVisitorName("Tanvi Deshmukh");
        gv2.setPhone("+91 98765 77772");
        gv2.setHost(resident2);
        gv2.setFlatNumber("A-105");
        gv2.setTotalGuests(2);
        gv2.setStatus("PENDING");
        groupVisitRepository.save(gv2);

        // Search for Aarav
        mockMvc.perform(get("/guard/dashboard").param("query", "Aarav"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("groupVisits", hasSize(1)))
                .andExpect(model().attribute("groupVisits", hasItem(hasProperty("primaryVisitorName", is("Aarav Gupta")))));

        // Search for flat A-105
        mockMvc.perform(get("/guard/dashboard").param("query", "A-105"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("groupVisits", hasSize(1)))
                .andExpect(model().attribute("groupVisits", hasItem(hasProperty("flatNumber", is("A-105")))));
    }

    // =========================================================================
    // Scenario 12: Guard cannot access admin user management APIs (403 Forbidden)
    // =========================================================================
    @Test
    @WithMockUser(username = "guard1", roles = {"GUARD"})
    @DisplayName("Scenario 12: Guard cannot invoke Admin User Management endpoints (403 Forbidden)")
    public void testGuardCannotAccessAdminUserApis() throws Exception {
        mockMvc.perform(post("/admin/users/create")
                        .with(csrf())
                        .param("username", "hacker")
                        .param("password", "pass123")
                        .param("role", "ROLE_ADMIN")
                        .param("name", "Malicious User")
                        .param("unitNumber", "Z-999"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users/" + resident1.getId() + "/update-role")
                        .with(csrf())
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());
    }
}

