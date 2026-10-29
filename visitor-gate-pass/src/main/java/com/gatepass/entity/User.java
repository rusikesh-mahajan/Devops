package com.gatepass.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_GUARD, ROLE_RESIDENT

    @Column(nullable = false)
    private String name;

    @Column(name = "unit_number")
    private String unitNumber; // Flat/apartment number (e.g., B-204, A-105)

    private String phone;

    public User() {}

    public User(String username, String password, String role, String name) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public User(String username, String password, String role, String name, String unitNumber, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.unitNumber = unitNumber;
        this.phone = phone;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnitNumber() { return unitNumber != null ? unitNumber : "N/A"; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    public String getPhone() { return phone != null ? phone : "N/A"; }
    public void setPhone(String phone) { this.phone = phone; }
}
