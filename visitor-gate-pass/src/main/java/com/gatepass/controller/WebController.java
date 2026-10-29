package com.gatepass.controller;

import com.gatepass.entity.GroupVisit;
import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
import com.gatepass.repository.GroupVisitRepository;
import com.gatepass.repository.UserRepository;
import com.gatepass.repository.VisitorPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class WebController {

    @Autowired
    private VisitorPassRepository passRepo;

    @Autowired
    private GroupVisitRepository groupRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model, Authentication auth) {
        if (auth != null) {
            model.addAttribute("username", auth.getName());
            String role = auth.getAuthorities().iterator().next().getAuthority();
            model.addAttribute("role", role);
            if ("ROLE_ADMIN".equals(role)) {
                model.addAttribute("redirectUrl", "/admin/dashboard");
            } else if ("ROLE_GUARD".equals(role)) {
                model.addAttribute("redirectUrl", "/guard/dashboard");
            } else {
                model.addAttribute("redirectUrl", "/resident/dashboard");
            }
        } else {
            model.addAttribute("redirectUrl", "/login");
        }
        return "access-denied";
    }

    // Common /dashboard route
    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String statusFilter,
                            @RequestParam(required = false) String query,
                            Model model, 
                            Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) {
            return adminDashboard(statusFilter, query, model, auth);
        } else if ("ROLE_GUARD".equals(role)) {
            return guardDashboard(statusFilter, query, model, auth);
        } else {
            return residentDashboard(statusFilter, query, model, auth);
        }
    }

    // ==========================================
    // 1. ADMIN DEDICATED DASHBOARD & MANAGEMENT
    // ==========================================
    @GetMapping("/admin/dashboard")
    public String adminDashboard(@RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) String query,
                                 Model model, 
                                 Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepo.findByUsername(username).orElse(null);

        model.addAttribute("role", "ROLE_ADMIN");
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", query);

        // Core Pass Metrics
        long totalPasses = passRepo.count();
        long pendingPasses = passRepo.findByStatus("PENDING").size();
        long approvedPasses = passRepo.findByStatus("APPROVED").size();
        long checkedInPasses = passRepo.findByStatus("CHECKED_IN").size();
        long checkedOutPasses = passRepo.findByStatus("CHECKED_OUT").size();
        long rejectedPasses = passRepo.findByStatus("REJECTED").size();

        // Group Metrics
        long totalGroups = groupRepo.count();
        long activeGroupsInside = groupRepo.countByStatus("CHECKED_IN");
        long activeGroupGuestsInside = groupRepo.sumActiveCheckedInGuests();
        long totalVisitorsInside = checkedInPasses + activeGroupGuestsInside;

        model.addAttribute("totalPasses", totalPasses);
        model.addAttribute("pendingPasses", pendingPasses);
        model.addAttribute("approvedPasses", approvedPasses);
        model.addAttribute("checkedInPasses", checkedInPasses);
        model.addAttribute("checkedOutPasses", checkedOutPasses);
        model.addAttribute("rejectedPasses", rejectedPasses);
        model.addAttribute("totalGroups", totalGroups);
        model.addAttribute("activeGroupsInside", activeGroupsInside);
        model.addAttribute("totalGroupsInside", activeGroupsInside);
        model.addAttribute("activeGroupGuestsInside", activeGroupGuestsInside);
        model.addAttribute("totalVisitorsInside", totalVisitorsInside);

        // User & Society Unit Metrics
        List<User> allUsers = userRepo.findAll();
        List<User> residents = userRepo.findByRole("ROLE_RESIDENT");
        List<User> guards = userRepo.findByRole("ROLE_GUARD");
        model.addAttribute("users", allUsers);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("residents", residents);
        model.addAttribute("guards", guards);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalResidents", residents.size());
        model.addAttribute("totalGuards", guards.size());

        // Residential Units Directory
        java.util.Map<String, User> unitsDirectory = residents.stream()
                .filter(u -> u.getUnitNumber() != null && !u.getUnitNumber().isBlank())
                .collect(java.util.stream.Collectors.toMap(User::getUnitNumber, u -> u, (existing, replacement) -> existing));
        model.addAttribute("unitsDirectory", unitsDirectory);

        // Daily and Weekly Visitor Stats
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        long todayCheckedInGuests = groupRepo.sumCheckedInGuestsToday(startOfDay);
        long todayCheckedInPasses = passRepo.findAll().stream()
                .filter(p -> p.getCheckInTime() != null && p.getCheckInTime().isAfter(startOfDay))
                .count();
        model.addAttribute("todayTotalCheckIns", todayCheckedInPasses + todayCheckedInGuests);

        long weekTotalCheckIns = passRepo.findAll().stream()
                .filter(p -> p.getCheckInTime() != null && p.getCheckInTime().isAfter(startOfWeek))
                .count();
        model.addAttribute("weekTotalCheckIns", weekTotalCheckIns);

        // Visitor Passes (Filtered or Full List)
        List<VisitorPass> passes;
        if (query != null && !query.trim().isEmpty()) {
            passes = passRepo.searchAllFields(query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            passes = passRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
        } else {
            passes = passRepo.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("passes", passes);

        // Group Visits (Filtered or Full List)
        List<GroupVisit> groupVisits;
        if (query != null && !query.trim().isEmpty()) {
            groupVisits = groupRepo.searchAllGroupVisits(query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            groupVisits = groupRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
        } else {
            groupVisits = groupRepo.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("groupVisits", groupVisits);

        return "admin-dashboard";
    }

    // Admin User Management Endpoints
    @PostMapping("/admin/users/create")
    public String createUserByAdmin(@RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam String name,
                                    @RequestParam String role,
                                    @RequestParam(required = false) String unitNumber,
                                    @RequestParam(required = false) String phone,
                                    RedirectAttributes redirectAttributes) {
        if (username == null || username.trim().length() < 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username must be at least 3 characters.");
            return "redirect:/admin/dashboard";
        }
        if (userRepo.findByUsername(username.trim()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + username + "' is already taken.");
            return "redirect:/admin/dashboard";
        }
        if (password == null || password.trim().length() < 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 4 characters.");
            return "redirect:/admin/dashboard";
        }

        User newUser = new User(
            username.trim(),
            passwordEncoder.encode(password.trim()),
            role.trim().toUpperCase(),
            name.trim(),
            unitNumber != null ? unitNumber.trim() : "N/A",
            phone != null ? phone.trim() : "N/A"
        );
        userRepo.save(newUser);
        redirectAttributes.addFlashAttribute("successMessage", "User '" + newUser.getUsername() + "' (" + newUser.getRole().replace("ROLE_", "") + ") created successfully.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/users/{id}/update-role")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/dashboard";
        }
        User user = userOpt.get();
        user.setRole(role.trim().toUpperCase());
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Role for user '" + user.getUsername() + "' updated to " + user.getRole().replace("ROLE_", ""));
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/dashboard";
        }
        User user = userOpt.get();
        if (user.getUsername().equalsIgnoreCase(auth.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete your own active administrator account.");
            return "redirect:/admin/dashboard";
        }
        userRepo.delete(user);
        redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getUsername() + "' removed from system.");
        return "redirect:/admin/dashboard";
    }

    // ==========================================
    // 2. SECURITY GUARD GATE OPERATIONS CONSOLE
    // ==========================================
    @GetMapping("/guard/dashboard")
    public String guardDashboard(@RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) String query,
                                 Model model, 
                                 Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepo.findByUsername(username).orElse(null);

        model.addAttribute("role", "ROLE_GUARD");
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", query);

        // Gate Operations Summary Cards
        long checkedInPasses = passRepo.findByStatus("CHECKED_IN").size();
        long activeGroupGuestsInside = groupRepo.sumActiveCheckedInGuests();
        long totalVisitorsInside = checkedInPasses + activeGroupGuestsInside; // Total visitors inside premises
        long totalGroupsInside = groupRepo.countByStatus("CHECKED_IN"); // Total groups inside
        long expectedGroupGuestsToday = groupRepo.sumExpectedGuests(); // Total guests expected today
        long approvedPasses = passRepo.findByStatus("APPROVED").size();
        long totalGuestsExpectedToday = approvedPasses + expectedGroupGuestsToday;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCheckedInGuests = groupRepo.sumCheckedInGuestsToday(startOfDay);
        long todayCheckedInPasses = passRepo.findAll().stream()
                .filter(p -> p.getCheckInTime() != null && p.getCheckInTime().isAfter(startOfDay))
                .count();
        long totalGuestsCheckedInToday = todayCheckedInPasses + todayCheckedInGuests;

        model.addAttribute("totalVisitorsInside", totalVisitorsInside);
        model.addAttribute("totalGuestsInsidePremises", totalVisitorsInside);
        model.addAttribute("totalGroupsInside", totalGroupsInside);
        model.addAttribute("totalGuestsExpectedToday", totalGuestsExpectedToday);
        model.addAttribute("totalGuestsCheckedInToday", totalGuestsCheckedInToday);

        // Core metric attributes for backwards-compatible test assertions
        model.addAttribute("totalPasses", passRepo.count() + groupRepo.count());
        model.addAttribute("pendingPasses", passRepo.findByStatus("PENDING").size() + groupRepo.findByStatus("PENDING").size());
        model.addAttribute("approvedPasses", approvedPasses);
        model.addAttribute("checkedInPasses", totalVisitorsInside);

        // Residents dropdown list for gate registration
        List<User> residents = userRepo.findByRole("ROLE_RESIDENT");
        model.addAttribute("residents", residents);
        model.addAttribute("residentsList", residents);

        // Group Visits Table Data
        List<GroupVisit> groupVisits;
        if (query != null && !query.trim().isEmpty()) {
            groupVisits = groupRepo.searchAllGroupVisits(query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            groupVisits = groupRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
        } else {
            groupVisits = groupRepo.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("groupVisits", groupVisits);

        // Individual Visitor Passes
        List<VisitorPass> passes;
        if (query != null && !query.trim().isEmpty()) {
            passes = passRepo.searchAllFields(query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            passes = passRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
        } else {
            passes = passRepo.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("passes", passes);

        return "guard-dashboard";
    }

    // Guard: Create Individual Pass
    @PostMapping("/guard/create-pass")
    public String createPassByGuard(@RequestParam String visitorName, 
                                    @RequestParam String phone,
                                    @RequestParam String purpose, 
                                    @RequestParam(required = false) String vehicleNo,
                                    @RequestParam Long hostId,
                                    RedirectAttributes redirectAttributes) {
        if (visitorName == null || visitorName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Visitor name must be at least 2 characters.");
            return "redirect:/dashboard";
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9. ()-]{7,25}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid phone number format.");
            return "redirect:/dashboard";
        }

        Optional<User> hostOpt = userRepo.findById(hostId);
        if (hostOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected resident host does not exist.");
            return "redirect:/dashboard";
        }

        VisitorPass pass = new VisitorPass();
        pass.setVisitorName(visitorName.trim());
        pass.setPhone(phone.trim());
        pass.setPurpose(purpose != null ? purpose.trim() : "General Visit");
        pass.setVehicleNo(vehicleNo != null ? vehicleNo.trim().toUpperCase() : "");
        pass.setStatus("PENDING");
        pass.setHost(hostOpt.get());

        passRepo.save(pass);
        redirectAttributes.addFlashAttribute("successMessage", 
            "Visitor pass created for " + pass.getVisitorName() + " (Pass Code: " + pass.getPassCode() + "). Awaiting resident approval.");
        return "redirect:/dashboard";
    }

    // Guard: Register Group Visit
    @PostMapping("/guard/create-group-visit")
    public String createGroupVisitByGuard(@RequestParam String primaryVisitorName,
                                          @RequestParam String phone,
                                          @RequestParam int totalGuests,
                                          @RequestParam(required = false) String accompanyingGuestNames,
                                          @RequestParam String purpose,
                                          @RequestParam(required = false) String vehicleNo,
                                          @RequestParam Long hostId,
                                          @RequestParam(required = false) String expectedArrivalTime,
                                          RedirectAttributes redirectAttributes) {
        // Business Rule: Total guests must be at least 1
        if (totalGuests < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Total guest count must be at least 1 (including primary visitor).");
            return "redirect:/dashboard";
        }
        if (primaryVisitorName == null || primaryVisitorName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Primary visitor name must be at least 2 characters.");
            return "redirect:/dashboard";
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9. ()-]{7,25}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid contact number format.");
            return "redirect:/dashboard";
        }

        Optional<User> hostOpt = userRepo.findById(hostId);
        if (hostOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected resident host does not exist.");
            return "redirect:/dashboard";
        }

        User host = hostOpt.get();
        GroupVisit group = new GroupVisit();
        group.setPrimaryVisitorName(primaryVisitorName.trim());
        group.setPhone(phone.trim());
        group.setTotalGuests(totalGuests); // Auto sets accompanyingGuests = totalGuests - 1
        group.setAccompanyingGuestNames(accompanyingGuestNames != null ? accompanyingGuestNames.trim() : "");
        group.setHost(host);
        group.setFlatNumber(host.getUnitNumber());
        group.setPurpose(purpose != null && !purpose.isBlank() ? purpose.trim() : "Group Visit");
        group.setVehicleNo(vehicleNo != null ? vehicleNo.trim().toUpperCase() : "");
        group.setStatus("PENDING");

        if (expectedArrivalTime != null && !expectedArrivalTime.isBlank()) {
            try {
                group.setExpectedArrivalTime(LocalDateTime.parse(expectedArrivalTime));
            } catch (Exception ignored) {
                group.setExpectedArrivalTime(LocalDateTime.now());
            }
        }

        groupRepo.save(group);
        redirectAttributes.addFlashAttribute("successMessage",
            "Group visit registered for " + group.getPrimaryVisitorName() + " (" + group.getTotalGuests() + " guests, " +
            group.getAccompanyingGuests() + " accompanying) to " + host.getName() + " (" + group.getFlatNumber() + "). Group ID: " + group.getGroupCode());

        return "redirect:/dashboard";
    }

    // Guard: Group Status Transition (Check In / Check Out)
    @PostMapping("/guard/group-status/{id}")
    public String updateGroupStatusGuard(@PathVariable Long id,
                                         @RequestParam String status,
                                         RedirectAttributes redirectAttributes) {
        Optional<GroupVisit> groupOpt = groupRepo.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group visit record not found.");
            return "redirect:/dashboard";
        }

        GroupVisit group = groupOpt.get();

        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            // Guard: Cannot check in pending or rejected
            if (!group.canCheckIn()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid Action: Only APPROVED groups can be checked in. Current status: " + group.getStatus());
                return "redirect:/dashboard";
            }
            // Prevent duplicate check-in
            if (group.getCheckInTime() != null && group.getCheckOutTime() == null && "CHECKED_IN".equalsIgnoreCase(group.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Duplicate Check-In: Group is already checked in.");
                return "redirect:/dashboard";
            }

            group.setStatus("CHECKED_IN");
            group.setCheckInTime(LocalDateTime.now());
            groupRepo.save(group);
            redirectAttributes.addFlashAttribute("successMessage",
                "Group " + group.getGroupCode() + " (" + group.getPrimaryVisitorName() + ", " + group.getTotalGuests() + " guests) checked in successfully.");

        } else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
            if (!group.canCheckOut()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid Action: Only CHECKED_IN groups can be checked out. Current status: " + group.getStatus());
                return "redirect:/dashboard";
            }

            group.setStatus("CHECKED_OUT");
            group.setCheckOutTime(LocalDateTime.now());
            groupRepo.save(group);
            redirectAttributes.addFlashAttribute("successMessage",
                "Group " + group.getGroupCode() + " (" + group.getTotalGuests() + " guests) checked out successfully.");

        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unsupported status update for group: " + status);
        }

        return "redirect:/dashboard";
    }

    // Guard: Single Pass Status Transition
    @PostMapping("/guard/update-status/{id}")
    public String updateStatusGuard(@PathVariable Long id, 
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        Optional<VisitorPass> passOpt = passRepo.findById(id);
        if (passOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pass record not found.");
            return "redirect:/dashboard";
        }

        VisitorPass pass = passOpt.get();

        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            if (!pass.canCheckIn()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Invalid Action: Only APPROVED passes can be checked in. Current status: " + pass.getStatus());
                return "redirect:/dashboard";
            }
            pass.setStatus("CHECKED_IN");
            pass.setCheckInTime(LocalDateTime.now());
            passRepo.save(pass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Visitor " + pass.getVisitorName() + " checked in successfully at " + pass.getCheckInTime().toLocalTime().toString().substring(0, 5));

        } else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
            if (!pass.canCheckOut()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Invalid Action: Only CHECKED_IN passes can be checked out. Current status: " + pass.getStatus());
                return "redirect:/dashboard";
            }
            pass.setStatus("CHECKED_OUT");
            pass.setCheckOutTime(LocalDateTime.now());
            passRepo.save(pass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Visitor " + pass.getVisitorName() + " checked out successfully.");

        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unsupported status update action: " + status);
        }

        return "redirect:/dashboard";
    }

    // ==========================================
    // 3. RESIDENT PORTAL & WORKFLOW ACTIONS
    // ==========================================
    @GetMapping("/resident/dashboard")
    public String residentDashboard(@RequestParam(required = false) String statusFilter,
                                    @RequestParam(required = false) String query,
                                    Model model, 
                                    Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepo.findByUsername(username).orElse(null);
        Long hostId = (currentUser != null) ? currentUser.getId() : -1L;

        model.addAttribute("role", "ROLE_RESIDENT");
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", query);

        // Scoped Passes for Resident
        List<VisitorPass> passes;
        if (query != null && !query.trim().isEmpty()) {
            passes = passRepo.searchHostPasses(hostId, query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            passes = passRepo.findByHostIdOrderByCreatedAtDesc(hostId).stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase(statusFilter))
                    .toList();
        } else {
            passes = passRepo.findByHostIdOrderByCreatedAtDesc(hostId);
        }
        model.addAttribute("passes", passes);

        // Scoped Groups for Resident
        List<GroupVisit> groupVisits;
        if (query != null && !query.trim().isEmpty()) {
            groupVisits = groupRepo.searchHostGroupVisits(hostId, query.trim());
        } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            groupVisits = groupRepo.findByHostIdOrderByCreatedAtDesc(hostId).stream()
                    .filter(g -> g.getStatus().equalsIgnoreCase(statusFilter))
                    .toList();
        } else {
            groupVisits = groupRepo.findByHostIdOrderByCreatedAtDesc(hostId);
        }
        model.addAttribute("groupVisits", groupVisits);

        // Resident Metrics
        long myTotal = passes.size() + groupVisits.size();
        long myPending = passes.stream().filter(p -> "PENDING".equals(p.getStatus())).count() +
                         groupVisits.stream().filter(g -> "PENDING".equals(g.getStatus())).count();
        long myApproved = passes.stream().filter(p -> "APPROVED".equals(p.getStatus())).count() +
                          groupVisits.stream().filter(g -> "APPROVED".equals(g.getStatus())).count();
        long myInside = passes.stream().filter(p -> "CHECKED_IN".equals(p.getStatus())).count() +
                        groupVisits.stream().filter(g -> "CHECKED_IN".equals(g.getStatus())).count();

        model.addAttribute("totalPasses", myTotal);
        model.addAttribute("pendingPasses", myPending);
        model.addAttribute("approvedPasses", myApproved);
        model.addAttribute("checkedInPasses", myInside);
        model.addAttribute("checkedOutPasses", passes.stream().filter(p -> "CHECKED_OUT".equals(p.getStatus())).count());
        model.addAttribute("rejectedPasses", passes.stream().filter(p -> "REJECTED".equals(p.getStatus())).count());

        return "dashboard"; // Renders standard dashboard customized with resident tabs
    }

    // Resident: Pre-Register Individual Guest
    @PostMapping("/resident/pre-register")
    public String preRegisterByResident(@RequestParam String visitorName, 
                                        @RequestParam String phone,
                                        @RequestParam String purpose, 
                                        @RequestParam(required = false) String vehicleNo,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {
        if (visitorName == null || visitorName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Visitor name must be at least 2 characters.");
            return "redirect:/dashboard";
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9. ()-]{7,25}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid phone number format.");
            return "redirect:/dashboard";
        }

        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Resident session error.");
            return "redirect:/dashboard";
        }

        VisitorPass pass = new VisitorPass();
        pass.setVisitorName(visitorName.trim());
        pass.setPhone(phone.trim());
        pass.setPurpose(purpose != null ? purpose.trim() : "Guest");
        pass.setVehicleNo(vehicleNo != null ? vehicleNo.trim().toUpperCase() : "");
        pass.setStatus("APPROVED"); // Pre-approved by resident!
        pass.setHost(currentUser);

        passRepo.save(pass);
        redirectAttributes.addFlashAttribute("successMessage", 
            "Pre-approved visitor pass generated for " + pass.getVisitorName() + " (Pass Code: " + pass.getPassCode() + "). Guard can check them in directly.");
        return "redirect:/dashboard";
    }

    // Resident: Pre-Register Group
    @PostMapping("/resident/pre-register-group")
    public String preRegisterGroupByResident(@RequestParam String primaryVisitorName,
                                             @RequestParam String phone,
                                             @RequestParam int totalGuests,
                                             @RequestParam(required = false) String accompanyingGuestNames,
                                             @RequestParam String purpose,
                                             @RequestParam(required = false) String vehicleNo,
                                             Authentication auth,
                                             RedirectAttributes redirectAttributes) {
        if (totalGuests < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Total guests must be at least 1.");
            return "redirect:/dashboard";
        }
        if (primaryVisitorName == null || primaryVisitorName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Primary visitor name must be at least 2 characters.");
            return "redirect:/dashboard";
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9. ()-]{7,25}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid contact number format.");
            return "redirect:/dashboard";
        }

        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Resident session error.");
            return "redirect:/dashboard";
        }

        GroupVisit group = new GroupVisit();
        group.setPrimaryVisitorName(primaryVisitorName.trim());
        group.setPhone(phone.trim());
        group.setTotalGuests(totalGuests);
        group.setAccompanyingGuestNames(accompanyingGuestNames != null ? accompanyingGuestNames.trim() : "");
        group.setPurpose(purpose != null && !purpose.isBlank() ? purpose.trim() : "Guest Group");
        group.setVehicleNo(vehicleNo != null ? vehicleNo.trim().toUpperCase() : "");
        group.setHost(currentUser);
        group.setFlatNumber(currentUser.getUnitNumber());
        group.setStatus("APPROVED"); // Pre-approved by resident!

        groupRepo.save(group);
        redirectAttributes.addFlashAttribute("successMessage",
            "Pre-approved group visit registered for " + group.getPrimaryVisitorName() + " (" + group.getTotalGuests() + " guests) to flat " + group.getFlatNumber() + ". Group ID: " + group.getGroupCode());

        return "redirect:/dashboard";
    }

    // Resident: Approve or Reject Pass
    @PostMapping("/resident/update-status/{id}")
    public String updateStatusResident(@PathVariable Long id, 
                                       @RequestParam String status,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        Optional<VisitorPass> passOpt = passRepo.findById(id);
        if (passOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pass record not found.");
            return "redirect:/dashboard";
        }

        VisitorPass pass = passOpt.get();
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);

        // Security check: Only host resident or admin can approve/reject
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (currentUser == null || !pass.getHost().getId().equals(currentUser.getId()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You can only approve/reject visitors for your own residence.");
            return "redirect:/dashboard";
        }

        if (!"PENDING".equalsIgnoreCase(pass.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot change status: Pass is already " + pass.getStatus());
            return "redirect:/dashboard";
        }

        if ("APPROVED".equalsIgnoreCase(status)) {
            pass.setStatus("APPROVED");
            passRepo.save(pass);
            redirectAttributes.addFlashAttribute("successMessage", "Visitor pass " + pass.getPassCode() + " has been APPROVED.");
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            pass.setStatus("REJECTED");
            passRepo.save(pass);
            redirectAttributes.addFlashAttribute("successMessage", "Visitor pass " + pass.getPassCode() + " has been REJECTED.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status action requested.");
        }

        return "redirect:/dashboard";
    }

    // Resident: Approve or Reject Group Visit
    @PostMapping("/resident/group-status/{id}")
    public String updateGroupStatusResident(@PathVariable Long id,
                                            @RequestParam String status,
                                            Authentication auth,
                                            RedirectAttributes redirectAttributes) {
        Optional<GroupVisit> groupOpt = groupRepo.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group record not found.");
            return "redirect:/dashboard";
        }

        GroupVisit group = groupOpt.get();
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (currentUser == null || !group.getHost().getId().equals(currentUser.getId()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You can only manage group visits for your own flat.");
            return "redirect:/dashboard";
        }

        if (!"PENDING".equalsIgnoreCase(group.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot change status: Group is already " + group.getStatus());
            return "redirect:/dashboard";
        }

        if ("APPROVED".equalsIgnoreCase(status)) {
            group.setStatus("APPROVED");
            groupRepo.save(group);
            redirectAttributes.addFlashAttribute("successMessage", "Group visit " + group.getGroupCode() + " has been APPROVED.");
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            group.setStatus("REJECTED");
            groupRepo.save(group);
            redirectAttributes.addFlashAttribute("successMessage", "Group visit " + group.getGroupCode() + " has been REJECTED.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status update action.");
        }

        return "redirect:/dashboard";
    }

    // Pass details edit
    @PostMapping("/pass/edit/{id}")
    public String editPass(@PathVariable Long id,
                           @RequestParam String visitorName,
                           @RequestParam String phone,
                           @RequestParam String purpose,
                           @RequestParam(required = false) String vehicleNo,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        Optional<VisitorPass> passOpt = passRepo.findById(id);
        if (passOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pass record not found.");
            return "redirect:/dashboard";
        }

        VisitorPass pass = passOpt.get();
        if (!pass.canEdit()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pass cannot be modified once checked in or closed.");
            return "redirect:/dashboard";
        }

        if (visitorName == null || visitorName.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Visitor name must be at least 2 characters.");
            return "redirect:/dashboard";
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9. ()-]{7,25}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid phone number format.");
            return "redirect:/dashboard";
        }

        pass.setVisitorName(visitorName.trim());
        pass.setPhone(phone.trim());
        pass.setPurpose(purpose != null ? purpose.trim() : pass.getPurpose());
        pass.setVehicleNo(vehicleNo != null ? vehicleNo.trim().toUpperCase() : pass.getVehicleNo());

        passRepo.save(pass);
        redirectAttributes.addFlashAttribute("successMessage", "Visitor pass " + pass.getPassCode() + " details updated successfully.");
        return "redirect:/dashboard";
    }

    // REST API Endpoints
    @GetMapping("/api/passes/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPassDetails(@PathVariable Long id) {
        Optional<VisitorPass> passOpt = passRepo.findById(id);
        if (passOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VisitorPass p = passOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("passCode", p.getPassCode());
        data.put("visitorName", p.getVisitorName());
        data.put("phone", p.getPhone());
        data.put("purpose", p.getPurpose());
        data.put("vehicleNo", p.getVehicleNo() != null ? p.getVehicleNo() : "N/A");
        data.put("status", p.getStatus());
        data.put("hostName", p.getHost() != null ? p.getHost().getName() : "Unknown");
        data.put("flatNumber", p.getHost() != null ? p.getHost().getUnitNumber() : "N/A");
        data.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "N/A");
        data.put("checkInTime", p.getCheckInTime() != null ? p.getCheckInTime().toString() : "Not Checked In");
        data.put("checkOutTime", p.getCheckOutTime() != null ? p.getCheckOutTime().toString() : "Not Checked Out");

        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/group-visits/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGroupVisitDetails(@PathVariable Long id) {
        Optional<GroupVisit> groupOpt = groupRepo.findById(id);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GroupVisit g = groupOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", g.getId());
        data.put("groupCode", g.getGroupCode());
        data.put("primaryVisitorName", g.getPrimaryVisitorName());
        data.put("phone", g.getPhone());
        data.put("totalGuests", g.getTotalGuests());
        data.put("accompanyingGuests", g.getAccompanyingGuests());
        data.put("accompanyingGuestNames", g.getAccompanyingGuestNames() != null ? g.getAccompanyingGuestNames() : "None");
        data.put("hostName", g.getHost() != null ? g.getHost().getName() : "Unknown");
        data.put("flatNumber", g.getFlatNumber());
        data.put("purpose", g.getPurpose());
        data.put("vehicleNo", g.getVehicleNo() != null ? g.getVehicleNo() : "N/A");
        data.put("status", g.getStatus());
        data.put("checkInTime", g.getCheckInTime() != null ? g.getCheckInTime().toString() : "Not Checked In");
        data.put("checkOutTime", g.getCheckOutTime() != null ? g.getCheckOutTime().toString() : "Not Checked Out");
        data.put("createdAt", g.getCreatedAt() != null ? g.getCreatedAt().toString() : "N/A");

        return ResponseEntity.ok(data);
    }

    @GetMapping("/admin/search")
    public String searchRedirect(@RequestParam(required = false) String query) {
        if (query != null && !query.trim().isEmpty()) {
            return "redirect:/admin/dashboard?query=" + query.trim();
        }
        return "redirect:/admin/dashboard";
    }
}
