package com.gatepass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gate_passes")
public class VisitorPass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pass_code", unique = true)
    private String passCode;

    @NotBlank(message = "Visitor name cannot be empty")
    @Size(min = 2, max = 100, message = "Visitor name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String visitorName;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number")
    @Column(nullable = false)
    private String phone;

    private String purpose;
    private String vehicleNo;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, CHECKED_IN, CHECKED_OUT

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    private LocalDateTime createdAt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (passCode == null || passCode.isBlank()) {
            passCode = "GP-" + String.format("%04d", (int)(Math.random() * 9000) + 1000);
        }
    }

    // Workflow validation helpers
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
}
