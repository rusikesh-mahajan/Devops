package com.gatepass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_visits")
public class GroupVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", unique = true, nullable = false)
    private String groupCode; // e.g. GV-001, GV-1002

    @NotBlank(message = "Primary visitor name cannot be empty")
    @Size(min = 2, max = 100, message = "Primary visitor name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String primaryVisitorName;

    @NotBlank(message = "Contact phone cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String phone;

    private String vehicleNo;
    private String purpose;

    @Min(value = 1, message = "Total guest count must be at least 1")
    @Column(name = "total_guests", nullable = false)
    private int totalGuests = 1; // Primary visitor + accompanying guests

    @Min(value = 0, message = "Accompanying guests count cannot be negative")
    @Column(name = "accompanying_guests", nullable = false)
    private int accompanyingGuests = 0; // Total guests - 1

    @Column(name = "accompanying_guest_names", columnDefinition = "TEXT")
    private String accompanyingGuestNames; // Optional comma or newline separated names

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "flat_number", nullable = false)
    private String flatNumber; // e.g. B-204, A-105

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, CHECKED_IN, CHECKED_OUT

    private LocalDateTime expectedArrivalTime;
    private LocalDateTime createdAt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public GroupVisit() {}

    public GroupVisit(String primaryVisitorName, String phone, int totalGuests, 
                      String accompanyingGuestNames, User host, String flatNumber, 
                      String purpose, String vehicleNo) {
        this.primaryVisitorName = primaryVisitorName;
        this.phone = phone;
        setGuestCounts(totalGuests);
        this.accompanyingGuestNames = accompanyingGuestNames;
        this.host = host;
        this.flatNumber = flatNumber != null ? flatNumber : (host != null ? host.getUnitNumber() : "N/A");
        this.purpose = purpose != null ? purpose : "Group Visit";
        this.vehicleNo = vehicleNo != null ? vehicleNo.trim().toUpperCase() : "";
        this.status = "PENDING";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (groupCode == null || groupCode.isBlank()) {
            groupCode = "GV-" + String.format("%03d", (int)(Math.random() * 900) + 100);
        }
        // Ensure accompanyingGuests is always strictly totalGuests - 1
        recalculateAccompanying();
        if (flatNumber == null || flatNumber.isBlank()) {
            flatNumber = (host != null) ? host.getUnitNumber() : "N/A";
        }
    }

    // Helper to keep totalGuests and accompanyingGuests strictly consistent
    public void setGuestCounts(int total) {
        if (total < 1) {
            throw new IllegalArgumentException("Total guest count must be at least 1 (including primary visitor)");
        }
        this.totalGuests = total;
        recalculateAccompanying();
    }

    public void recalculateAccompanying() {
        this.accompanyingGuests = Math.max(0, this.totalGuests - 1);
    }

    // State Machine Guards
    public boolean canApprove() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean canReject() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean canCheckIn() {
        return "APPROVED".equalsIgnoreCase(this.status);
    }

    public boolean canCheckOut() {
        return "CHECKED_IN".equalsIgnoreCase(this.status);
    }

    public boolean canEdit() {
        return "PENDING".equalsIgnoreCase(this.status) || "APPROVED".equalsIgnoreCase(this.status);
    }

    // Checked-in / Checked-out guest counts for dashboard display
    public int getCheckedInCount() {
        return "CHECKED_IN".equalsIgnoreCase(this.status) ? this.totalGuests : 0;
    }

    public int getCheckedOutCount() {
        return "CHECKED_OUT".equalsIgnoreCase(this.status) ? this.totalGuests : 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getPrimaryVisitorName() { return primaryVisitorName; }
    public void setPrimaryVisitorName(String primaryVisitorName) { this.primaryVisitorName = primaryVisitorName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public int getTotalGuests() { return totalGuests; }
    public void setTotalGuests(int totalGuests) { setGuestCounts(totalGuests); }

    public int getAccompanyingGuests() { return accompanyingGuests; }
    public void setAccompanyingGuests(int accompanyingGuests) { this.accompanyingGuests = accompanyingGuests; }

    public String getAccompanyingGuestNames() { return accompanyingGuestNames; }
    public void setAccompanyingGuestNames(String accompanyingGuestNames) { this.accompanyingGuestNames = accompanyingGuestNames; }

    public User getHost() { return host; }
    public void setHost(User host) { 
        this.host = host; 
        if (host != null && (this.flatNumber == null || this.flatNumber.isBlank())) {
            this.flatNumber = host.getUnitNumber();
        }
    }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getExpectedArrivalTime() { return expectedArrivalTime; }
    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) { this.expectedArrivalTime = expectedArrivalTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
}

