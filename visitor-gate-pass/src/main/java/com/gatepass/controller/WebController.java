package com.gatepass.controller;

import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
import com.gatepass.repository.UserRepository;
import com.gatepass.repository.VisitorPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class WebController {

    @Autowired
    private VisitorPassRepository passRepo;
    
    @Autowired
    private UserRepository userRepo;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String statusFilter,
                            @RequestParam(required = false) String query,
                            Model model, 
                            Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        String username = auth.getName();
        User currentUser = userRepo.findByUsername(username).orElse(null);

        model.addAttribute("role", role);
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", query);

        // Dashboard Metrics (Calculated for Admin, Guard & Resident)
        long totalPasses = passRepo.count();
        long pendingPasses = passRepo.findByStatus("PENDING").size();
        long approvedPasses = passRepo.findByStatus("APPROVED").size();
        long checkedInPasses = passRepo.findByStatus("CHECKED_IN").size();
        long checkedOutPasses = passRepo.findByStatus("CHECKED_OUT").size();
        long rejectedPasses = passRepo.findByStatus("REJECTED").size();

        model.addAttribute("totalPasses", totalPasses);
        model.addAttribute("pendingPasses", pendingPasses);
        model.addAttribute("approvedPasses", approvedPasses);
        model.addAttribute("checkedInPasses", checkedInPasses);
        model.addAttribute("checkedOutPasses", checkedOutPasses);
        model.addAttribute("rejectedPasses", rejectedPasses);

        List<VisitorPass> passes;

        if ("ROLE_ADMIN".equals(role)) {
            if (query != null && !query.trim().isEmpty()) {
                passes = passRepo.searchAllFields(query.trim());
            } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                passes = passRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
            } else {
                passes = passRepo.findAllByOrderByCreatedAtDesc();
            }
            model.addAttribute("passes", passes);
            model.addAttribute("residents", userRepo.findByRole("ROLE_RESIDENT"));

        } else if ("ROLE_GUARD".equals(role)) {
            if (query != null && !query.trim().isEmpty()) {
                passes = passRepo.searchAllFields(query.trim());
            } else if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                passes = passRepo.findByStatusOrderByCreatedAtDesc(statusFilter.toUpperCase());
            } else {
                passes = passRepo.findAllByOrderByCreatedAtDesc();
            }
            model.addAttribute("passes", passes);
            model.addAttribute("residents", userRepo.findByRole("ROLE_RESIDENT"));

        } else if ("ROLE_RESIDENT".equals(role)) {
            Long hostId = (currentUser != null) ? currentUser.getId() : -1L;
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
        }

        return "dashboard";
    }

    @PostMapping("/guard/create-pass")
    public String createPassByGuard(@RequestParam String visitorName, 
                                    @RequestParam String phone,
                                    @RequestParam String purpose, 
                                    @RequestParam(required = false) String vehicleNo,
                                    @RequestParam Long hostId,
                                    RedirectAttributes redirectAttributes) {
        // Server-side validation
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

        // State Machine validation
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

        // State Machine transitions:
        // APPROVED -> CHECKED_IN
        // CHECKED_IN -> CHECKED_OUT
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

        // Check if editable
        if (!pass.canEdit()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pass cannot be modified once checked in or closed.");
            return "redirect:/dashboard";
        }

        // Validate
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
        data.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "N/A");
        data.put("checkInTime", p.getCheckInTime() != null ? p.getCheckInTime().toString() : "Not Checked In");
        data.put("checkOutTime", p.getCheckOutTime() != null ? p.getCheckOutTime().toString() : "Not Checked Out");

        return ResponseEntity.ok(data);
    }

    @GetMapping("/admin/search")
    public String searchRedirect(@RequestParam(required = false) String query) {
        if (query != null && !query.trim().isEmpty()) {
            return "redirect:/dashboard?query=" + query.trim();
        }
        return "redirect:/dashboard";
    }
}
